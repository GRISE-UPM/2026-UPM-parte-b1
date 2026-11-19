package es.upm.grise.subscriptionService;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import SubscriptionService;
import User;
import Delivery;
import Message;

import es.upm.grise.subscriptionService.exceptions.ExistingUserException;
import es.upm.grise.subscriptionService.exceptions.NonExistingUserException;
import es.upm.grise.subscriptionService.exceptions.NullUserException;
import es.upm.grise.subscriptionService.exceptions.UserDoesNotHaveEmailException;

class SubscriptionServiceTest {

	@Test
	public void smokeTest() {

		SubscriptionService sr = mock(SubscriptionService.class);

		// comporbar correcta inicializacion
		when(sr.getSubscribers()).thenReturn(null);

		// crear usuarios
		User user_null = new User(EMAIL, null);
		User user1 = new User(EMAIL, "user1@gmail.com");
		User user_local = new User(LOCAL, null);
		User user2 = new User(EMAIL, "user2@gmail.com");
		User user_donot = new User(DO_NOT_DELIVER, null);

		sr.addSubscriber(user1);

		// comprobar las excepciones en addSubscriber
		when(sr.addSubscriber(null)).thenReturn(NullUserException.class);
		when(sr.addSubscriber(user1)).thenReturn(ExistingUserException.class);
		when(sr.addSubscriber(user_null)).thenReturn(UserDoesNotHaveEmailException.class);
		when(sr.addSubscriber(user_local)).thenReturn();

		// comprobar las excepciones de removeSubscriber
		when(sr.removeSubscriber(null)).thenReturn(NullUserException.class);
		when(sr.removeSubscriber(user2)).thenReturn(NonExistingUserException.class);

		Message mg1 = new Message(1, "Message 1");
		Message mg2 = new Message(2, "Message 2");
		Message mg3 = new Message(3, "Message 3");

		when(sr.sendMessage(mg1)).thenReturn(0);
		sr.addSubscriber(user_donot);
		when(sr.sendMessage(msg2)).thenReturn(1);
		sr.removeSubscriber(user_donot);
		when(sr.sendMessage(msg3)).thenReturn(0);e

	}

}
