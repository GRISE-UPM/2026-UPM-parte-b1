package es.upm.grise.subscriptionService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import es.upm.grise.subscriptionService.exceptions.ExistingUserException;
import es.upm.grise.subscriptionService.exceptions.NonExistingUserException;
import es.upm.grise.subscriptionService.exceptions.NullUserException;
import es.upm.grise.subscriptionService.exceptions.UserDoesNotHaveEmailException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class SubscriptionServiceTest {

    private EmailService emailService;
    private SubscriptionService service;
    private User user;
    private Message message;

    @BeforeEach
    void setUp() {
        emailService = mock(EmailService.class);
        service = new SubscriptionService(emailService);
        user = mock(User.class);
        message = mock(Message.class);
    }

    @Test
    public void smokeTest() {
        assertTrue(service.getSubscribers().isEmpty());
    }

    /* -------------------- addSubscriber -------------------- */

    @Test
    void addSubscriber_nullUser_throwsNullUserException() {
        assertThrows(NullUserException.class, () -> service.addSubscriber(null));
    }

    @Test
    void addSubscriber_existingUser_throwsExistingUserException() throws Exception {
        when(user.getDelivery()).thenReturn(Delivery.EMAIL);
        when(user.getEmail()).thenReturn("user@example.com");
        service.addSubscriber(user);

        assertThrows(ExistingUserException.class, () -> service.addSubscriber(user));
    }

    @Test
    void addSubscriber_emailNullAndDeliveryEmail_throwsUserDoesNotHaveEmailException() {
        when(user.getDelivery()).thenReturn(Delivery.EMAIL);
        when(user.getEmail()).thenReturn(null);

        assertThrows(UserDoesNotHaveEmailException.class, () -> service.addSubscriber(user));
    }

    @Test
    void addSubscriber_emailNullAndDeliveryDoNotDeliver_throwsUserDoesNotHaveEmailException() {
        when(user.getDelivery()).thenReturn(Delivery.DO_NOT_DELIVER);
        when(user.getEmail()).thenReturn(null);

        assertThrows(UserDoesNotHaveEmailException.class, () -> service.addSubscriber(user));
    }

    @Test
    void addSubscriber_emailNullAndDeliveryLocal_addsUser() throws Exception {
        when(user.getDelivery()).thenReturn(Delivery.LOCAL);
        when(user.getEmail()).thenReturn(null);

        service.addSubscriber(user);

        assertTrue(service.getSubscribers().contains(user));
    }

    @Test
    void addSubscriber_validUser_addsUser() throws Exception {
        when(user.getDelivery()).thenReturn(Delivery.EMAIL);
        when(user.getEmail()).thenReturn("user@example.com");

        service.addSubscriber(user);

        assertTrue(service.getSubscribers().contains(user));
    }

    /* -------------------- removeSubscriber -------------------- */

    @Test
    void removeSubscriber_nullUser_throwsNullUserException() {
        assertThrows(NullUserException.class, () -> service.removeSubscriber(null));
    }

    @Test
    void removeSubscriber_nonExistingUser_throwsNonExistingUserException() {
        assertThrows(NonExistingUserException.class, () -> service.removeSubscriber(user));
    }

    @Test
    void removeSubscriber_existingUser_removesUser() throws Exception {
        when(user.getDelivery()).thenReturn(Delivery.EMAIL);
        when(user.getEmail()).thenReturn("user@example.com");
        service.addSubscriber(user);

        service.removeSubscriber(user);

        assertTrue(service.getSubscribers().isEmpty());
    }

    /* -------------------- sendMessage -------------------- */

    @Test
    void sendMessage_noSubscribers_returnsZero() {
        assertEquals(0, service.sendMessage(message));
        verify(emailService, never()).sendMessage(any(), any());
    }

    @Test
    void sendMessage_deliveryEmail_sendsEmailAndReturnsZero() throws Exception {
        when(user.getDelivery()).thenReturn(Delivery.EMAIL);
        when(user.getEmail()).thenReturn("user@example.com");
        service.addSubscriber(user);

        int discarded = service.sendMessage(message);

        assertEquals(0, discarded);
        verify(emailService, times(1)).sendMessage(user, message);
        verify(user, never()).saveMessage(any());
    }

    @Test
    void sendMessage_deliveryLocal_savesMessageAndReturnsZero() throws Exception {
        when(user.getDelivery()).thenReturn(Delivery.LOCAL);
        service.addSubscriber(user);

        int discarded = service.sendMessage(message);

        assertEquals(0, discarded);
        verify(user, times(1)).saveMessage(message);
        verify(emailService, never()).sendMessage(any(), any());
    }

    @Test
    void sendMessage_deliveryDoNotDeliver_savesNoticeAndReturnsOne() throws Exception {
        when(user.getDelivery()).thenReturn(Delivery.DO_NOT_DELIVER);
        when(user.getEmail()).thenReturn("user@example.com");
        service.addSubscriber(user);

        int discarded = service.sendMessage(message);

        assertEquals(1, discarded);
        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(user).saveMessage(captor.capture());
        // Si Message expone getMessage()/getText(), descomenta y ajusta:
        // assertEquals("Ha perdido ud. un mensaje", captor.getValue().getMessage());
        verify(emailService, never()).sendMessage(any(), any());
    }

    @Test
    void sendMessage_mixedUsers_returnsCorrectDiscardedCount() throws Exception {
        User emailUser = mock(User.class);
        User localUser = mock(User.class);
        User doNotDeliverUser = mock(User.class);

        when(emailUser.getDelivery()).thenReturn(Delivery.EMAIL);
        when(emailUser.getEmail()).thenReturn("email@example.com");
        when(localUser.getDelivery()).thenReturn(Delivery.LOCAL);
        when(doNotDeliverUser.getDelivery()).thenReturn(Delivery.DO_NOT_DELIVER);
        when(doNotDeliverUser.getEmail()).thenReturn("dn@example.com");

        service.addSubscriber(emailUser);
        service.addSubscriber(localUser);
        service.addSubscriber(doNotDeliverUser);

        int discarded = service.sendMessage(message);

        assertEquals(1, discarded);
        verify(emailService, times(1)).sendMessage(emailUser, message);
        verify(localUser, times(1)).saveMessage(message);
        verify(doNotDeliverUser, times(1)).saveMessage(any(Message.class));
    }
}