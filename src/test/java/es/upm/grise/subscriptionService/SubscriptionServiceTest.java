package es.upm.grise.subscriptionService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import es.upm.grise.subscriptionService.exceptions.ExistingUserException;
import es.upm.grise.subscriptionService.exceptions.NonExistingUserException;
import es.upm.grise.subscriptionService.exceptions.NullUserException;
import es.upm.grise.subscriptionService.exceptions.UserDoesNotHaveEmailException;

class SubscriptionServiceTest {

	private static final String EMAIL = "usuario@upm.es";
	private static final String TEXTO_MENSAJE_PERDIDO = "Ha perdido ud. un mensaje";

	private EmailService emailService;
	private SubscriptionService service;
	private Message message;

	@BeforeEach
	void setUp() {
		emailService = mock(EmailService.class);
		service = new SubscriptionService(emailService);
		message = new Message(1L, "Hola");
	}

	private static User crearUsuario(Delivery delivery, String email) {
		User user = mock(User.class);
		when(user.getDelivery()).thenReturn(delivery);
		when(user.getEmail()).thenReturn(email);
		return user;
	}

	// Lee el atributo privado "message" de Message
	private static Object textoDe(Message m) throws Exception {
		Field campo = Message.class.getDeclaredField("message");
		campo.setAccessible(true);
		return campo.get(m);
	}

	@Test
	void constructor_listaDeSuscriptoresVacia() {
		assertNotNull(service.getSubscribers());
		assertTrue(service.getSubscribers().isEmpty());
	}

	@Test
	void constructor_noUsaEmailService() {
		verifyNoInteractions(emailService);
	}

	@Test
	void addSubscriber_anadeUsuarioEmail() throws Exception {
		User user = crearUsuario(Delivery.EMAIL, EMAIL);

		service.addSubscriber(user);

		assertEquals(1, service.getSubscribers().size());
		assertTrue(service.getSubscribers().contains(user));
	}

	@Test
	void addSubscriber_anadeUsuarioDoNotDeliverConEmail() throws Exception {
		User user = crearUsuario(Delivery.DO_NOT_DELIVER, EMAIL);

		service.addSubscriber(user);

		assertTrue(service.getSubscribers().contains(user));
	}

	@Test
	void addSubscriber_anadeVariosUsuarios() throws Exception {
		User u1 = crearUsuario(Delivery.EMAIL, EMAIL);
		User u2 = crearUsuario(Delivery.LOCAL, null);
		User u3 = crearUsuario(Delivery.DO_NOT_DELIVER, EMAIL);

		service.addSubscriber(u1);
		service.addSubscriber(u2);
		service.addSubscriber(u3);

		assertEquals(3, service.getSubscribers().size());
		assertTrue(service.getSubscribers().contains(u1));
		assertTrue(service.getSubscribers().contains(u2));
		assertTrue(service.getSubscribers().contains(u3));
	}

	@Test
	void addSubscriber_usuarioNullLanzaNullUserException() {
		assertThrows(NullUserException.class, () -> service.addSubscriber(null));
		assertTrue(service.getSubscribers().isEmpty());
	}

	@Test
	void addSubscriber_usuarioYaSuscritoLanzaExistingUserException() throws Exception {
		User user = crearUsuario(Delivery.EMAIL, EMAIL);
		service.addSubscriber(user);

		assertThrows(ExistingUserException.class, () -> service.addSubscriber(user));
		assertEquals(1, service.getSubscribers().size());
	}

	@Test
	void addSubscriber_usuarioLocalYaSuscritoLanzaExistingUserException() throws Exception {
		User user = crearUsuario(Delivery.LOCAL, null);
		service.addSubscriber(user);

		assertThrows(ExistingUserException.class, () -> service.addSubscriber(user));
		assertEquals(1, service.getSubscribers().size());
	}

	@Test
	void addSubscriber_usuarioEmailSinEmailLanzaUserDoesNotHaveEmailException() {
		User user = crearUsuario(Delivery.EMAIL, null);

		assertThrows(UserDoesNotHaveEmailException.class, () -> service.addSubscriber(user));
		assertFalse(service.getSubscribers().contains(user));
	}

	@Test
	void addSubscriber_usuarioDoNotDeliverSinEmailLanzaUserDoesNotHaveEmailException() {
		User user = crearUsuario(Delivery.DO_NOT_DELIVER, null);

		assertThrows(UserDoesNotHaveEmailException.class, () -> service.addSubscriber(user));
		assertFalse(service.getSubscribers().contains(user));
	}

	@Test
	void addSubscriber_usuarioLocalSinEmailSeAnade() throws Exception {
		User user = crearUsuario(Delivery.LOCAL, null);

		service.addSubscriber(user);

		assertTrue(service.getSubscribers().contains(user));
	}

	@Test
	void addSubscriber_usuarioLocalNoSeConsultaEmail() throws Exception {
		User user = crearUsuario(Delivery.LOCAL, null);

		service.addSubscriber(user);

		verify(user, never()).getEmail();
	}

	@Test
	void addSubscriber_usuarioLocalConEmailSeAnade() throws Exception {
		User user = crearUsuario(Delivery.LOCAL, EMAIL);

		service.addSubscriber(user);

		assertTrue(service.getSubscribers().contains(user));
	}

	@Test
	void removeSubscriber_eliminaUsuarioSuscrito() throws Exception {
		User user = crearUsuario(Delivery.EMAIL, EMAIL);
		service.addSubscriber(user);

		service.removeSubscriber(user);

		assertFalse(service.getSubscribers().contains(user));
		assertTrue(service.getSubscribers().isEmpty());
	}

	@Test
	void removeSubscriber_eliminaSoloElUsuarioIndicado() throws Exception {
		User u1 = crearUsuario(Delivery.EMAIL, EMAIL);
		User u2 = crearUsuario(Delivery.LOCAL, null);
		service.addSubscriber(u1);
		service.addSubscriber(u2);

		service.removeSubscriber(u1);

		assertEquals(1, service.getSubscribers().size());
		assertFalse(service.getSubscribers().contains(u1));
		assertTrue(service.getSubscribers().contains(u2));
	}

	@Test
	void removeSubscriber_usuarioNullLanzaNullUserException() {
		assertThrows(NullUserException.class, () -> service.removeSubscriber(null));
	}

	@Test
	void removeSubscriber_usuarioNullNoModificaLista() throws Exception {
		User user = crearUsuario(Delivery.EMAIL, EMAIL);
		service.addSubscriber(user);

		assertThrows(NullUserException.class, () -> service.removeSubscriber(null));
		assertEquals(1, service.getSubscribers().size());
	}

	@Test
	void removeSubscriber_listaVaciaLanzaNonExistingUserException() {
		User user = crearUsuario(Delivery.EMAIL, EMAIL);

		assertThrows(NonExistingUserException.class, () -> service.removeSubscriber(user));
	}

	@Test
	void removeSubscriber_usuarioNoSuscritoLanzaNonExistingUserException() throws Exception {
		User suscrito = crearUsuario(Delivery.EMAIL, EMAIL);
		User noSuscrito = crearUsuario(Delivery.EMAIL, EMAIL);
		service.addSubscriber(suscrito);

		assertThrows(NonExistingUserException.class, () -> service.removeSubscriber(noSuscrito));
		assertEquals(1, service.getSubscribers().size());
		assertTrue(service.getSubscribers().contains(suscrito));
	}

	@Test
	void removeSubscriber_eliminarDosVecesLanzaNonExistingUserException() throws Exception {
		User user = crearUsuario(Delivery.EMAIL, EMAIL);
		service.addSubscriber(user);
		service.removeSubscriber(user);

		assertThrows(NonExistingUserException.class, () -> service.removeSubscriber(user));
	}

	@Test
	void removeSubscriber_usuarioEliminadoPuedeVolverASuscribirse() throws Exception {
		User user = crearUsuario(Delivery.EMAIL, EMAIL);
		service.addSubscriber(user);
		service.removeSubscriber(user);

		service.addSubscriber(user);

		assertTrue(service.getSubscribers().contains(user));
	}

	@Test
	void sendMessage_sinSuscriptoresDevuelve0() {
		assertEquals(0, service.sendMessage(message));
		verifyNoInteractions(emailService);
	}

	@Test
	void sendMessage_usuarioEmailRecibePorEmailService() throws Exception {
		User user = crearUsuario(Delivery.EMAIL, EMAIL);
		service.addSubscriber(user);

		int descartados = service.sendMessage(message);

		verify(emailService, times(1)).sendMessage(user, message);
		verify(user, never()).saveMessage(any());
		assertEquals(0, descartados);
	}

	@Test
	void sendMessage_usuarioLocalGuardaMensaje() throws Exception {
		User user = crearUsuario(Delivery.LOCAL, null);
		service.addSubscriber(user);

		int descartados = service.sendMessage(message);

		verify(user, times(1)).saveMessage(message);
		verifyNoInteractions(emailService);
		assertEquals(0, descartados);
	}

	@Test
	void sendMessage_usuarioDoNotDeliverRecibeMensajePerdido() throws Exception {
		User user = crearUsuario(Delivery.DO_NOT_DELIVER, EMAIL);
		service.addSubscriber(user);

		int descartados = service.sendMessage(message);

		ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
		verify(user, times(1)).saveMessage(captor.capture());
		Message guardado = captor.getValue();

		assertNotSame(message, guardado);
		assertEquals(TEXTO_MENSAJE_PERDIDO, textoDe(guardado));
		assertEquals(message.getId(), guardado.getId());
		verifyNoInteractions(emailService);
		assertEquals(1, descartados);
	}

	@Test
	void sendMessage_todosLosUsuariosEmailReciben() throws Exception {
		User u1 = crearUsuario(Delivery.EMAIL, EMAIL);
		User u2 = crearUsuario(Delivery.EMAIL, "otro@upm.es");
		service.addSubscriber(u1);
		service.addSubscriber(u2);

		int descartados = service.sendMessage(message);

		verify(emailService).sendMessage(u1, message);
		verify(emailService).sendMessage(u2, message);
		verify(emailService, times(2)).sendMessage(any(), any());
		assertEquals(0, descartados);
	}

	@Test
	void sendMessage_devuelveNumeroDeDescartados() throws Exception {
		service.addSubscriber(crearUsuario(Delivery.DO_NOT_DELIVER, EMAIL));
		service.addSubscriber(crearUsuario(Delivery.DO_NOT_DELIVER, "b@upm.es"));
		service.addSubscriber(crearUsuario(Delivery.DO_NOT_DELIVER, "c@upm.es"));

		assertEquals(3, service.sendMessage(message));
	}

	@Test
	void sendMessage_suscriptoresMixtos() throws Exception {
		User email = crearUsuario(Delivery.EMAIL, EMAIL);
		User local = crearUsuario(Delivery.LOCAL, null);
		User noEntregar1 = crearUsuario(Delivery.DO_NOT_DELIVER, "a@upm.es");
		User noEntregar2 = crearUsuario(Delivery.DO_NOT_DELIVER, "b@upm.es");
		service.addSubscriber(email);
		service.addSubscriber(local);
		service.addSubscriber(noEntregar1);
		service.addSubscriber(noEntregar2);

		int descartados = service.sendMessage(message);

		assertEquals(2, descartados);
		verify(emailService, times(1)).sendMessage(email, message);
		verify(emailService, times(1)).sendMessage(any(), any());
		verify(email, never()).saveMessage(any());
		verify(local, times(1)).saveMessage(message);
		verify(noEntregar1, times(1)).saveMessage(any());
		verify(noEntregar2, times(1)).saveMessage(any());
	}

	@Test
	void sendMessage_usuarioEliminadoNoRecibe() throws Exception {
		User user = crearUsuario(Delivery.EMAIL, EMAIL);
		service.addSubscriber(user);
		service.removeSubscriber(user);

		int descartados = service.sendMessage(message);

		verifyNoInteractions(emailService);
		assertEquals(0, descartados);
	}

	@Test
	void sendMessage_contadorNoSeAcumulaEntreEnvios() throws Exception {
		service.addSubscriber(crearUsuario(Delivery.DO_NOT_DELIVER, EMAIL));

		assertEquals(1, service.sendMessage(message));
		assertEquals(1, service.sendMessage(new Message(2L, "Adiós")));
	}
}