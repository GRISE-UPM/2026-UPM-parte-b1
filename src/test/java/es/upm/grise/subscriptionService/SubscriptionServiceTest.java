package es.upm.grise.subscriptionService;

import es.upm.grise.subscriptionService.exceptions.ExistingUserException;
import es.upm.grise.subscriptionService.exceptions.NonExistingUserException;
import es.upm.grise.subscriptionService.exceptions.NullUserException;
import es.upm.grise.subscriptionService.exceptions.UserDoesNotHaveEmailException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class SubscriptionServiceTest {

	@Test
	public void smokeTest() {
		EmailService mockInterface = Mockito.mock(EmailService.class);
		SubscriptionService service = new SubscriptionService(mockInterface);
		assertNotNull(service);
	}


	@Test
	public void addSubscriberTest() throws NullUserException, UserDoesNotHaveEmailException, ExistingUserException {
		EmailService mockInterface = Mockito.mock(EmailService.class);
		SubscriptionService service = new SubscriptionService(mockInterface);
		assertThrows(NullUserException.class, () -> service.addSubscriber(null));

		User localUser = new User(Delivery.LOCAL, null);
		User localEmailUser = new User(Delivery.LOCAL, "localEmail");
		User nonLocalUser = new User(Delivery.EMAIL, null);
		User nonLocalEmailUser = new User(Delivery.EMAIL, "nonLocalEmail");

		service.addSubscriber(localUser);
		service.addSubscriber(localEmailUser);
		assertThrows(UserDoesNotHaveEmailException.class, () -> service.addSubscriber(nonLocalUser));
		service.addSubscriber(nonLocalEmailUser);
		assertThrows(ExistingUserException.class, () -> service.addSubscriber(localUser));

		assertTrue(service.getSubscribers().contains(localUser)
				&& service.getSubscribers().contains(localEmailUser)
				&& service.getSubscribers().contains(nonLocalEmailUser)
		);
	}

	@Test
	public void removeSubscriberTest() throws NullUserException, UserDoesNotHaveEmailException, ExistingUserException, NonExistingUserException {
		EmailService mockInterface = Mockito.mock(EmailService.class);
		SubscriptionService service = new SubscriptionService(mockInterface);

		User existingSubscribed = new User(Delivery.LOCAL, null);
		User existingNonSubscribed = new User(Delivery.LOCAL, null);

		service.addSubscriber(existingSubscribed);

		assertThrows(NullUserException.class, () -> service.removeSubscriber(null));
		assertThrows(NonExistingUserException.class, () -> service.removeSubscriber(existingNonSubscribed));
		service.removeSubscriber(existingSubscribed);

		assertTrue(service.getSubscribers().isEmpty());
	}

	@Test
	public void sendMessageTest() throws NullUserException, UserDoesNotHaveEmailException, ExistingUserException {
		EmailService mockInterface = Mockito.mock(EmailService.class);
		SubscriptionService service = new SubscriptionService(mockInterface);

		User localUser = new User(Delivery.LOCAL, null);
		User localEmailUser = new User(Delivery.LOCAL, "localEmail");
		User noSendUser = new User(Delivery.DO_NOT_DELIVER, "noDeliveryEmail");
		User EmailUser = new User(Delivery.EMAIL, "Email");

		service.addSubscriber(localUser);
		service.addSubscriber(localEmailUser);
		service.addSubscriber(EmailUser);
		service.addSubscriber(noSendUser);

		int missedMessages = service.sendMessage(new Message(0, "new Message! Hello!"));

		assertEquals(missedMessages, 1);
		assertEquals(localUser.receivedMessages(), 1);
		assertEquals(localEmailUser.receivedMessages(), 1);
		assertEquals(EmailUser.receivedMessages(), 0);

	}
}
