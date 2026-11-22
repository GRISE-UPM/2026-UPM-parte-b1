package es.upm.grise.subscriptionService;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import es.upm.grise.subscriptionService.exceptions.ExistingUserException;
import es.upm.grise.subscriptionService.exceptions.NonExistingUserException;
import es.upm.grise.subscriptionService.exceptions.NullUserException;
import es.upm.grise.subscriptionService.exceptions.UserDoesNotHaveEmailException;

class SubscriptionServiceTest {

	
	
	
	
    @Test
	public void initializationTest() {
		
		EmailService emailService= Mockito.mock(EmailService.class);
		SubscriptionService subscriptionService = new SubscriptionService(emailService);
		assertTrue(subscriptionService.getSubscribers().isEmpty());

	}
	
	@Test
	public void Test1() {
		
		EmailService emailService= Mockito.mock(EmailService.class);
		SubscriptionService subscriptionService = new SubscriptionService(emailService);
		assertThrows(NullUserException.class, () -> subscriptionService.addSubscriber(null));

	}

	@Test
	public void Test2() throws ExistingUserException, NullUserException, UserDoesNotHaveEmailException {
		
		EmailService emailService= Mockito.mock(EmailService.class);
		SubscriptionService subscriptionService = new SubscriptionService(emailService);
		User user = spy(new User(Delivery.EMAIL, "ejemplo@upm.es"));
        subscriptionService.addSubscriber(user);
		assertThrows(ExistingUserException.class, () -> subscriptionService.addSubscriber(user));

	}

	@Test
	public void Test3()  {
		
		EmailService emailService= Mockito.mock(EmailService.class);
		SubscriptionService subscriptionService = new SubscriptionService(emailService);
		User user = spy(new User(Delivery.EMAIL, null));
		assertThrows(UserDoesNotHaveEmailException.class, () -> subscriptionService.addSubscriber(user));
	}

	@Test
	public void Test4()  {
		
		EmailService emailService= Mockito.mock(EmailService.class);
		SubscriptionService subscriptionService = new SubscriptionService(emailService);
		User user = spy(new User(Delivery.LOCAL, null));
		assertDoesNotThrow(() -> subscriptionService.addSubscriber(user));
		assertTrue(subscriptionService.getSubscribers().contains(user));
	}

	@Test
	public void Test5() throws NullUserException, ExistingUserException, UserDoesNotHaveEmailException, NonExistingUserException  {
		
		EmailService emailService= Mockito.mock(EmailService.class);
		SubscriptionService subscriptionService = new SubscriptionService(emailService);
		User user = spy(new User(Delivery.EMAIL, "ejemplo@upm.es"));
		subscriptionService.addSubscriber(user);
		subscriptionService.removeSubscriber(user);
		assertFalse(subscriptionService.getSubscribers().contains(user));
	}

	@Test
	public void Test6()  {
		
		EmailService emailService= Mockito.mock(EmailService.class);
		SubscriptionService subscriptionService = new SubscriptionService(emailService);
		assertThrows(NullUserException.class, () -> subscriptionService.removeSubscriber(null));
		
	}

	@Test
	public void Test7()  {
		
		EmailService emailService= Mockito.mock(EmailService.class);
		SubscriptionService subscriptionService = new SubscriptionService(emailService);
		User user = spy(new User(Delivery.EMAIL, "ejemplo@upm.es"));
		assertThrows(NonExistingUserException.class, () -> subscriptionService.removeSubscriber(user));
		
	}

	@Test
	public void Test8() throws NullUserException, ExistingUserException, UserDoesNotHaveEmailException  {
		
		EmailService emailService= Mockito.mock(EmailService.class);
		SubscriptionService subscriptionService = new SubscriptionService(emailService);
		User user = spy(new User(Delivery.EMAIL, "ejemplo@upm.es"));
		subscriptionService.addSubscriber(user);
		Message message = new Message(1, "Cuerpo");
		subscriptionService.sendMessage(message);
		verify(emailService).sendMessage(user, message);
	}

	@Test
	public void Test9() throws NullUserException, ExistingUserException, UserDoesNotHaveEmailException  {
		
		EmailService emailService= Mockito.mock(EmailService.class);
		SubscriptionService subscriptionService = new SubscriptionService(emailService);
		User user = spy(new User(Delivery.LOCAL, "ejemplo@upm.es"));
		subscriptionService.addSubscriber(user);
		Message message = new Message(1, "Cuerpo");
		subscriptionService.sendMessage(message);
		verify(emailService, times(0)).sendMessage(user, message);	
		verify(user).saveMessage(message);
	}

	@Test
	public void Test10() throws NullUserException, ExistingUserException, UserDoesNotHaveEmailException  {
		
		EmailService emailService= Mockito.mock(EmailService.class);
		SubscriptionService subscriptionService = new SubscriptionService(emailService);
		User user = spy(new User(Delivery.DO_NOT_DELIVER, "ejemplo@upm.es"));
		subscriptionService.addSubscriber(user);
		Message message = new Message(1, "Cuerpo");
		assertTrue(subscriptionService.sendMessage(message)==1);
		verify(emailService, times(0)).sendMessage(user, message);
	}

}