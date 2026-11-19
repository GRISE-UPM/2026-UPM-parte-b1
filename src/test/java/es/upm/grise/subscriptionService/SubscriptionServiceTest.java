package es.upm.grise.subscriptionService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
	void smokeTest() {
		assertTrue(subscriptionService.getSubscribers().isEmpty());
	}

	@Test
	void addSubscriberValidEmailUser() throws Exception {
		User user = mock(User.class);
		when(user.getDelivery()).thenReturn(Delivery.EMAIL);
		when(user.getEmail()).thenReturn("usuario@ejemplo.com");

		subscriptionService.addSubscriber(user);

		assertTrue(subscriptionService.getSubscribers().contains(user));
		assertEquals(1, subscriptionService.getSubscribers().size());
	}

	@Test
	void addSubscriberLocalUserWithoutEmailAllowed() throws Exception {
		User user = mock(User.class);
		when(user.getDelivery()).thenReturn(Delivery.LOCAL);
		when(user.getEmail()).thenReturn(null);

		subscriptionService.addSubscriber(user);

		assertTrue(subscriptionService.getSubscribers().contains(user));
	}

	@Test
	void addSubscriberNullThrowsNullUserException() {
		assertThrows(NullUserException.class, () -> subscriptionService.addSubscriber(null));
	}

	@Test
	void addExistingSubscriberThrowsExistingUserException() throws Exception {
		User user = mock(User.class);
		when(user.getDelivery()).thenReturn(Delivery.EMAIL);
		when(user.getEmail()).thenReturn("usuario@ejemplo.com");

		subscriptionService.addSubscriber(user);

		assertThrows(ExistingUserException.class, () -> subscriptionService.addSubscriber(user));
	}

	@Test
	void addNonLocalSubscriberWithoutEmailThrowsUserDoesNotHaveEmailException() {
		User user = mock(User.class);
		when(user.getDelivery()).thenReturn(Delivery.EMAIL);
		when(user.getEmail()).thenReturn(null);

		assertThrows(UserDoesNotHaveEmailException.class, () -> subscriptionService.addSubscriber(user));
	}

	@Test
	void removeExistingSubscriber() throws Exception {
		User user = mock(User.class);
		when(user.getDelivery()).thenReturn(Delivery.LOCAL);

		subscriptionService.addSubscriber(user);
		subscriptionService.removeSubscriber(user);

		assertFalse(subscriptionService.getSubscribers().contains(user));
		assertTrue(subscriptionService.getSubscribers().isEmpty());
	}

	@Test
	void removeNullSubscriberThrowsNullUserException() {
		assertThrows(NullUserException.class, () -> subscriptionService.removeSubscriber(null));
	}

	@Test
	void removeNonExistingSubscriberThrowsNonExistingUserException() {
		User user = mock(User.class);

		assertThrows(NonExistingUserException.class, () -> subscriptionService.removeSubscriber(user));
	}

	@Test
	void sendMessageDeliversAccordingToDeliveryTypeAndCountsDiscarded() throws Exception {
		User emailUser = mock(User.class);
		when(emailUser.getDelivery()).thenReturn(Delivery.EMAIL);
		when(emailUser.getEmail()).thenReturn("email@ejemplo.com");

		User localUser = mock(User.class);
		when(localUser.getDelivery()).thenReturn(Delivery.LOCAL);

		User doNotDeliverUser = mock(User.class);
		when(doNotDeliverUser.getDelivery()).thenReturn(Delivery.DO_NOT_DELIVER);
		when(doNotDeliverUser.getEmail()).thenReturn("nondelivery@ejemplo.com");

		subscriptionService.addSubscriber(emailUser);
		subscriptionService.addSubscriber(localUser);
		subscriptionService.addSubscriber(doNotDeliverUser);

		Message message = mock(Message.class);
		when(message.getId()).thenReturn(42L);
		when(message.getMessage()).thenReturn("Hola mundo");

		int discarded = subscriptionService.sendMessage(message);

		assertEquals(1, discarded);
		verify(emailService).sendMessage(emailUser, message);
		verify(emailService, never()).sendMessage(eq(localUser), argThat(m -> true));
		verify(emailService, never()).sendMessage(eq(doNotDeliverUser), argThat(m -> true));
		verify(localUser).saveMessage(message);
		verify(doNotDeliverUser).saveMessage(argThat(
				msg -> msg != null && msg.getId() == 42L && "Ha perdido ud. un mensaje".equals(msg.getMessage())));
	}

}