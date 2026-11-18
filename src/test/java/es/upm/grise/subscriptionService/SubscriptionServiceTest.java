package es.upm.grise.subscriptionService;

import org.junit.jupiter.api.Test;

class SubscriptionServiceTest {

	@Test
void testAddSubscriber_NullUser_ThrowsNullUserException() {
    assertThrows(NullUserException.class, () -> {
        subscriptionService.addSubscriber(null);
    });
}

@Test
void testAddSubscriber_LocalDeliveryNullEmail_Success() {
    User user = new User(Delivery.LOCAL, null);
	assertDoesNotThrow(() -> {
        subscriptionService.addSubscriber(user);
    });
    assertTrue(subscriptionService.getSubscribers().contains(user));
}

@Test
void testSendMessage_DoNotDeliver_StoresSpecialMessage() {
    User user = new User(Delivery.DO_NOT_DELIVER, "test@example.com");
    subscriptionService.addSubscriber(user);

    Message message = new Message(1L, "Contenido original de prueba");
    int discardedCount = subscriptionService.sendMessage(message);

    assertEquals(1, discardedCount);
    
    boolean containsSpecialMessage = user.getMessages().stream()
        .anyMatch(m -> m.getMessage().equals("Ha perdido ud. un mensaje"));
    assertTrue(containsSpecialMessage);
}

}
