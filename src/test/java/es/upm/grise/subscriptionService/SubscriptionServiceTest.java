package es.upm.grise.subscriptionService;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import es.upm.grise.subscriptionService.exceptions.ExistingUserException;
import es.upm.grise.subscriptionService.exceptions.NonExistingUserException;
import es.upm.grise.subscriptionService.exceptions.NullUserException;
import es.upm.grise.subscriptionService.exceptions.UserDoesNotHaveEmailException;

class SubscriptionServiceTest {

	@Test
	public void smokeTest() {}

	@Test
	void testInicializacionListaVacia(){
	
		EmailService emailServiceMock = mock(EmailService.class);
		SubscriptionService service = new SubscriptionService(emailServiceMock);
		
		assertTrue(service.getSubscribers().isEmpty());
	}
	
	@Test
	void testAddSubscriberNullUserException(){
	
		EmailService emailServiceMock = mock(EmailService.class);
		SubscriptionService service = new SubscriptionService(emailServiceMock);
		
		assertThrows(NullUserException.class, () -> {
			service.addSubscriber(null);
		});
	
	}
	
	@Test
	void testAddSubscriberExistingUserException(){
	
		EmailService emailServiceMock = mock(EmailService.class);
		SubscriptionService service = new SubscriptionService(emailServiceMock);
		User user = new User(Delivery.EMAIL, "usuario@upm.es");
		
		service.addSubscriber(user);
		
		assertThrows(ExistingUserException.class, () -> {
			service.addSubscriber(null);
		});	
	}
	
	@Test
	void testAddSubscriberUserDoesNotHaveEmailException(){
	
		EmailService emailServiceMock = mock(EmailService.class);
		SubscriptionService service = new SubscriptionService(emailServiceMock);
		User user = new User(Delivery.EMAIL, null);
		
		assertThrows(UserDoesNotHaveEmailException.class, () -> {
			service.addSubscriber(user);
		});
	
	}
	
	@Test
	void testAddSubscriberLocalSinEmailValido() throws Exception{
		EmailService emailServiceMock = mock(EmailService.class);
		SubscriptionService service = new SubscriptionService(emailServiceMock);
		User user = new User(Delivery.LOCAL, null);
		
		service.addSubscriber(user);
		
		assertTrue(service.getSubscribers().contains(user));
	}
	
	@Test
	void testAddSubscriberDoNotDeliverySinEmailEsValido() throws Exception{
		EmailService emailServiceMock = mock(EmailService.class);
		SubscriptionService service = new SubscriptionService(emailServiceMock);
		User user = new User(Delivery.DO_NOT_DELIVER, null);
		
		service.addSubscriber(user);
		
		assertTrue(service.getSubscribers().contains(user));
	}
	
	@Test
	void testAddSubscriberCorrecto(){
		EmailService emailServiceMock = mock(EmailService.class);
		SubscriptionService service = new SubscriptionService(emailServiceMock);
		User user = new User(Delivery.EMAIL, "usuario@upm.es");
		
		service.addSubscriber(user);
		
		assertTrue(service.getSubscribers().contains(user));
	
	}
	
	@Test
	void testRemoveSubscriberNullUserException() throws Exception{
	
		EmailService emailServiceMock = mock(EmailService.class);
		SubscriptionService service = new SubscriptionService(emailServiceMock);
		
		assertThrows(NullUserException.class, () -> {
			service.removeSubscriber(null);
		});
		
	}
	
	@Test
	void testRemoveSubscriberNonExistingUserException() throws Exception{
	
		EmailService emailServiceMock = mock(EmailService.class);
		SubscriptionService service = new SubscriptionService(emailServiceMock);
		User user = new User(Delivery.EMAIL, "usuario@upm.es");
		
		assertThrows(NonExistingUserException.class, () -> {
			service.removeSubscriber(user);
		});
		
	}
	
	
	@Test
	void testRemoveSubscriberValido() throws Exception{
		EmailService emailServiceMock = mock(EmailService.class);
		SubscriptionService service = new SubscriptionService(emailServiceMock);
		User user = new User(Delivery.EMAIL, "usuario@upm.es");
		
		service.addSubscriber(user);
		service.removeSubscriber(user);
		
		assertTrue(service.getSubscribers().isEmpty());
	}
	
	@Test
	void testSendMessageValid(){

		EmailService emailServiceMock = mock(EmailService.class);
		SubscriptionService service = new SubscriptionService(emailServiceMock);
		User user = new User(Delivery.EMAIL, "usuario@upm.es");			
		Message message = new Message(1L, "Hola mundo");
		
		service.addSubscriber(user);
				
		verify(emailServiceMock).sendMessage(user, message);
	
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

}
