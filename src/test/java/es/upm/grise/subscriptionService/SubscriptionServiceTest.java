package es.upm.grise.subscriptionService;

import es.upm.grise.subscriptionService.exceptions.*;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class SubscriptionServiceTest {

	@Test
	public void initialSubscribersEmpty() {
		EmailService emailService = mock(EmailService.class);
		SubscriptionService service = new SubscriptionService(emailService);
		assertTrue(service.getSubscribers().isEmpty());
	}

	@Test
	public void addNullUser() {
		EmailService emailService = mock(EmailService.class);
		SubscriptionService service = new SubscriptionService(emailService);
		assertThrows(NullUserException.class, () -> service.addSubscriber(null));
	}

	@Test
	public void addExistingUser() throws Exception {
		EmailService emailService = mock(EmailService.class);
		SubscriptionService service = new SubscriptionService(emailService);
		User user = new User(Delivery.EMAIL, "user@example.com");
		service.addSubscriber(user);
		assertThrows(ExistingUserException.class, () -> service.addSubscriber(user));
	}

	@Test
	public void addUserWithEmail() throws Exception {
		EmailService emailService = mock(EmailService.class);
		SubscriptionService service = new SubscriptionService(emailService);
		User user = new User(Delivery.EMAIL, "user@example.com");
		service.addSubscriber(user);
		assertTrue(service.getSubscribers().contains(user));
		assertEquals(1, service.getSubscribers().size());
	}
	
	@Test
	public void addEmailUserWithoutEmail() {
		EmailService emailService = mock(EmailService.class);
		SubscriptionService service = new SubscriptionService(emailService);
		User user = new User(Delivery.EMAIL, null);
		assertThrows(UserDoesNotHaveEmailException.class, () -> service.addSubscriber(user));
	}

	@Test
	public void addUserWithoutEmailWhenDeliveryIsLocal() throws Exception {
		EmailService emailService = mock(EmailService.class);
		SubscriptionService service = new SubscriptionService(emailService);
		User user = new User(Delivery.LOCAL, null);
		service.addSubscriber(user);
		assertTrue(service.getSubscribers().contains(user));
		assertEquals(1, service.getSubscribers().size());
	}

	@Test
	public void removeSubscriber() throws Exception {
		EmailService emailService = mock(EmailService.class);
		SubscriptionService service = new SubscriptionService(emailService);
		User user = new User(Delivery.LOCAL, null);
		service.addSubscriber(user);
		service.removeSubscriber(user);
		assertFalse(service.getSubscribers().contains(user));
		assertTrue(service.getSubscribers().isEmpty());
	}

	@Test
	public void removeNullSubscriber() {
		EmailService emailService = mock(EmailService.class);
		SubscriptionService service = new SubscriptionService(emailService);
		assertThrows(NullUserException.class, () -> service.removeSubscriber(null));
	}

	@Test
	public void removeNonExistingSubscriber() {
		EmailService emailService = mock(EmailService.class);
		SubscriptionService service = new SubscriptionService(emailService);
		User user = new User(Delivery.EMAIL, "user@example.com");
		assertThrows(NonExistingUserException.class, () -> service.removeSubscriber(user));
	}

	@Test
	public void sendMessageEmail() throws Exception {
		EmailService emailService = mock(EmailService.class);
		SubscriptionService service = new SubscriptionService(emailService);
		User user = new User(Delivery.EMAIL, "user@example.com");
		Message message = new Message(1, "Hola");
		service.addSubscriber(user);
		assertEquals(0, service.sendMessage(message));
		verify(emailService).sendMessage(user, message);
	}

	@Test
	public void sendMessageLocal() throws Exception {
		EmailService emailService = mock(EmailService.class);
		SubscriptionService service = new SubscriptionService(emailService);
		User user = new User(Delivery.LOCAL, null);
		Message message = new Message(1, "Hola");
		service.addSubscriber(user);
		assertEquals(0, service.sendMessage(message));
	}

	@Test
	public void sendMessageDoNotDeliver() throws Exception {
		EmailService emailService = mock(EmailService.class);
		SubscriptionService service = new SubscriptionService(emailService);
		User user = new User(Delivery.DO_NOT_DELIVER, "user@example.com");
		Message message = new Message(1, "Hola");
		service.addSubscriber(user);
		assertEquals(1, service.sendMessage(message));
	}

	@Test
	public void sendMessageNoSubscribers() {
		EmailService emailService = mock(EmailService.class);
		SubscriptionService service = new SubscriptionService(emailService);
		Message message = new Message(1, "Hola");
		assertEquals(0, service.sendMessage(message));
	}
}