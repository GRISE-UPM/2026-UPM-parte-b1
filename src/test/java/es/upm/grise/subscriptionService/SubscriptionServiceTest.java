package es.upm.grise.subscriptionService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.upm.grise.subscriptionService.exceptions.ExistingUserException;
import es.upm.grise.subscriptionService.exceptions.NonExistingUserException;
import es.upm.grise.subscriptionService.exceptions.NullUserException;
import es.upm.grise.subscriptionService.exceptions.UserDoesNotHaveEmailException;

class SubscriptionServiceTest {

	private EmailService emailService;
	private SubscriptionService subscriptionService;

	@BeforeEach
	void setUp() {
		emailService = mock(EmailService.class);
		subscriptionService = new SubscriptionService(emailService);
	}

	@Test
	void addSubscriberAcceptsEmailAndLocalUsers() throws Exception {
		User emailUser = new User(Delivery.EMAIL, "user@example.com");
		User localUser = new User(Delivery.LOCAL, null);

		subscriptionService.addSubscriber(emailUser);
		subscriptionService.addSubscriber(localUser);

		assertEquals(2, subscriptionService.getSubscribers().size());
		assertTrue(subscriptionService.getSubscribers().contains(emailUser));
		assertTrue(subscriptionService.getSubscribers().contains(localUser));
	}

	@Test
	void addSubscriberRejectsNullUser() {
		assertThrows(NullUserException.class, () -> subscriptionService.addSubscriber(null));
		assertTrue(subscriptionService.getSubscribers().isEmpty());
	}

	@Test
	void addSubscriberRejectsDuplicateUser() throws Exception {
		User user = new User(Delivery.LOCAL, null);
		subscriptionService.addSubscriber(user);

		assertThrows(ExistingUserException.class, () -> subscriptionService.addSubscriber(user));
		assertEquals(1, subscriptionService.getSubscribers().size());
	}

	@Test
	void addSubscriberRequiresEmailForNonLocalUsers() {
		for (Delivery delivery : new Delivery[] { Delivery.EMAIL, Delivery.DO_NOT_DELIVER }) {
			SubscriptionService service = new SubscriptionService(emailService);
			User userWithoutEmail = new User(delivery, null);

			assertThrows(UserDoesNotHaveEmailException.class, () -> service.addSubscriber(userWithoutEmail));
			assertTrue(service.getSubscribers().isEmpty());
		}
	}

	@Test
	void removeSubscriberRemovesExistingUser() throws Exception {
		User user = new User(Delivery.LOCAL, null);
		subscriptionService.addSubscriber(user);

		subscriptionService.removeSubscriber(user);

		assertFalse(subscriptionService.getSubscribers().contains(user));
	}

	@Test
	void removeSubscriberRejectsNullUser() {
		assertThrows(NullUserException.class, () -> subscriptionService.removeSubscriber(null));
	}

	@Test
	void removeSubscriberRejectsUnknownUser() {
		User user = new User(Delivery.LOCAL, null);

		assertThrows(NonExistingUserException.class, () -> subscriptionService.removeSubscriber(user));
	}

	@Test
	void sendMessageUsesEachDeliveryModeAndCountsDiscardedMessages() throws Exception {
		User localUser = mock(User.class);
		User emailUser = mock(User.class);
		User doNotDeliverUser = mock(User.class);
		when(localUser.getDelivery()).thenReturn(Delivery.LOCAL);
		when(emailUser.getDelivery()).thenReturn(Delivery.EMAIL);
		when(doNotDeliverUser.getDelivery()).thenReturn(Delivery.DO_NOT_DELIVER);
		when(emailUser.getEmail()).thenReturn("email@example.com");
		when(doNotDeliverUser.getEmail()).thenReturn("nodeliver@example.com");
		subscriptionService.addSubscriber(localUser);
		subscriptionService.addSubscriber(emailUser);
		subscriptionService.addSubscriber(doNotDeliverUser);
		Message message = new Message(42L, "Test message");

		int discardedMessages = subscriptionService.sendMessage(message);

		assertEquals(1, discardedMessages);
		verify(localUser).saveMessage(message);
		verify(emailService).sendMessage(emailUser, message);
		verify(doNotDeliverUser).saveMessage(org.mockito.ArgumentMatchers.argThat(
				discarded -> discarded.getId() == message.getId()));
		verifyNoMoreInteractions(emailService);
	}
}
