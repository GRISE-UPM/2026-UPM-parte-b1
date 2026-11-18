package es.upm.grise.subscriptionService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.spy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

import es.upm.grise.subscriptionService.exceptions.ExistingUserException;
import es.upm.grise.subscriptionService.exceptions.NonExistingUserException;
import es.upm.grise.subscriptionService.exceptions.NullUserException;
import es.upm.grise.subscriptionService.exceptions.UserDoesNotHaveEmailException;

import static org.mockito.Mockito.any;

class SubscriptionServiceTest {

    @Test
    void sendsEmailToEmailSubscriber()
            throws NullUserException, ExistingUserException, UserDoesNotHaveEmailException {
        EmailService emailMock = mock(EmailService.class);
        SubscriptionService service = new SubscriptionService(emailMock);
        User user = new User(Delivery.EMAIL, "ana@example.com");
        Message message = new Message(1L, "Hola");

        service.addSubscriber(user);
        int discardedMessages = service.sendMessage(message);

        assertEquals(0, discardedMessages);
        verify(emailMock).sendMessage(user, message);
    }

	@Test
	void throwsNullUserExceptionWhenAddingNullUser() {
    	EmailService emailMock = mock(EmailService.class);
    	SubscriptionService service = new SubscriptionService(emailMock);
    	assertThrows(NullUserException.class,() -> service.addSubscriber(null));
	}

	@Test
    void createDupedUser()
            throws NullUserException, ExistingUserException, UserDoesNotHaveEmailException {
        EmailService emailMock = mock(EmailService.class);
        SubscriptionService service = new SubscriptionService(emailMock);
        User user = new User(Delivery.EMAIL, "ana@example.com");
        Message message = new Message(1L, "Hola");

        service.addSubscriber(user);
		assertThrows(ExistingUserException.class,() -> service.addSubscriber(user));
    }

	@Test
    void emaillessUser()
            throws NullUserException, ExistingUserException, UserDoesNotHaveEmailException {
        EmailService emailMock = mock(EmailService.class);
        SubscriptionService service = new SubscriptionService(emailMock);
        User user = new User(Delivery.EMAIL, null);
        Message message = new Message(1L, "Hola");

		assertThrows(UserDoesNotHaveEmailException.class,() -> service.addSubscriber(user));
    }

	@Test
    void emaillessUserLocal()
            throws NullUserException, ExistingUserException, UserDoesNotHaveEmailException {
        EmailService emailMock = mock(EmailService.class);
        SubscriptionService service = new SubscriptionService(emailMock);
        User user = new User(Delivery.LOCAL, null);
        Message message = new Message(1L, "Hola");

		assertDoesNotThrow(() -> service.addSubscriber(user));
    }

	@Test
    void eliminarUser()
            throws NullUserException, ExistingUserException, UserDoesNotHaveEmailException {
        EmailService emailMock = mock(EmailService.class);
        SubscriptionService service = new SubscriptionService(emailMock);
        User user = new User(Delivery.LOCAL, null);
        Message message = new Message(1L, "Hola");
		service.addSubscriber(user);
		assertDoesNotThrow(() -> service.removeSubscriber(user));
    }

	@Test
    void eliminarUserNonExistent()
            throws NullUserException, ExistingUserException, UserDoesNotHaveEmailException {
        EmailService emailMock = mock(EmailService.class);
        SubscriptionService service = new SubscriptionService(emailMock);
        User user = new User(Delivery.LOCAL, null);
        Message message = new Message(1L, "Hola");

		assertThrows(NonExistingUserException.class,() -> service.removeSubscriber(user));
    }

	@Test
    void eliminarUserNull()
            throws NullUserException, ExistingUserException, UserDoesNotHaveEmailException {
        EmailService emailMock = mock(EmailService.class);
        SubscriptionService service = new SubscriptionService(emailMock);

		assertThrows(NullUserException.class,() -> service.removeSubscriber(null));
    }

	 @Test
    void sendsEmailToLocalSubscriber()
            throws NullUserException, ExistingUserException, UserDoesNotHaveEmailException {
        EmailService emailMock = mock(EmailService.class);
        SubscriptionService service = new SubscriptionService(emailMock);
        User user = spy(new User(Delivery.LOCAL, "ana@example.com"));
        Message message = new Message(1L, "Hola");

        service.addSubscriber(user);
        int discardedMessages = service.sendMessage(message);

        assertEquals(0, discardedMessages);
		verify(user).saveMessage(message);
    }

	 @Test
    void sendsEmailToDontDeliverSubscriber()
            throws NullUserException, ExistingUserException, UserDoesNotHaveEmailException {
        EmailService emailMock = mock(EmailService.class);
        SubscriptionService service = new SubscriptionService(emailMock);
        User user = spy(new User(Delivery.DO_NOT_DELIVER, "ana@example.com"));
        Message message = new Message(1L, "Hola");

        service.addSubscriber(user);
        int discardedMessages = service.sendMessage(message);

        assertEquals(1, discardedMessages);
		verify(user).saveMessage(any(Message.class));
    } 
}