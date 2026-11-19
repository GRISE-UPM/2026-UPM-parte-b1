package es.upm.grise.subscriptionService;

import es.upm.grise.subscriptionService.exceptions.NullUserException;
import es.upm.grise.subscriptionService.exceptions.ExistingUserException;
import es.upm.grise.subscriptionService.exceptions.UserDoesNotHaveEmailException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.mockito.Mockito.mock;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
class SubscriptionServiceTest {
	private EmailService emailService;
	private SubscriptionService subscription;

	@BeforeEach 
	void setUp() {
		emailService = mock(EmailService.class);
		subscription = new SubscriptionService(emailService);
	}

	@Test 
	void shouldAddLocalSuscriber() throws Exception {
		User user = new User(Delivery.LOCAL,null);
		
		subscription.addSubscriber(user);

		assertEquals(1, subscription.getSubscribers().size());


		// Test the subscribe method
	}

	@Test
	void shoulRejectNullSubscriber(){
		assertThrows(
			NullUserException.class,
			() -> subscription.addSubscriber(null)
		);
	}

	@Test 
	void shoudRejectDuplicateSubscriber() throws Exception {
		User user = new User(Delivery.LOCAL,null);
		subscription.addSubscriber(user);

		assertThrows(
			ExistingUserException.class,
			() -> subscription.addSubscriber(user)
		);
	}
	
	@Test
	void shouldRejectSubscriberWithoutEmail() {
		User user = new User(Delivery.EMAIL,null);

		assertThrows(
			UserDoesNotHaveEmailException.class,
			() -> subscription.addSubscriber(user)
		);
	}
	@Test 
	void shouldRemoveExistingSubscriber() throws Exception {
		User user = new User(Delivery.LOCAL,null);
		subscription.addSubscriber(user);

		subscription.removeSubscriber(user);

		assertEquals(0, subscription.getSubscribers().size());
	}
}