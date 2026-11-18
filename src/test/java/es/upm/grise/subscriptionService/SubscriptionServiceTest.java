package es.upm.grise.subscriptionService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import es.upm.grise.subscriptionService.exceptions.ExistingUserException;
import es.upm.grise.subscriptionService.exceptions.NonExistingUserException;
import es.upm.grise.subscriptionService.exceptions.NullUserException;
import es.upm.grise.subscriptionService.exceptions.UserDoesNotHaveEmailException;

class SubscriptionServiceTest {

	private final EmailService correo = mock(EmailService.class);
	private final SubscriptionService servicio = new SubscriptionService(correo);

	@Test
	void admiteUnSuscriptorLocalSinCorreo() throws Exception {
		User usuario = new User(Delivery.LOCAL, null);

		servicio.addSubscriber(usuario);

		assertEquals(1, servicio.getSubscribers().size());
		assertTrue(servicio.getSubscribers().contains(usuario));
	}

	@Test
	void rechazaUnAltaNula() {
		assertThrows(NullUserException.class, () -> servicio.addSubscriber(null));
		assertTrue(servicio.getSubscribers().isEmpty());
	}

	@Test
	void rechazaUnaSegundaAltaDelMismoUsuario() throws Exception {
		User usuario = new User(Delivery.EMAIL, "ana@example.com");
		servicio.addSubscriber(usuario);

		assertThrows(ExistingUserException.class, () -> servicio.addSubscriber(usuario));
		assertEquals(1, servicio.getSubscribers().size());
	}

	@Test
	void exigeCorreoParaLasModalidadesNoLocales() {
		User porCorreo = new User(Delivery.EMAIL, null);
		User sinEntrega = new User(Delivery.DO_NOT_DELIVER, null);

		assertThrows(UserDoesNotHaveEmailException.class, () -> servicio.addSubscriber(porCorreo));
		assertThrows(UserDoesNotHaveEmailException.class, () -> servicio.addSubscriber(sinEntrega));
		assertTrue(servicio.getSubscribers().isEmpty());
	}

	@Test
	void eliminaSoloAlUsuarioQueSeDaDeBaja() throws Exception {
		User baja = new User(Delivery.LOCAL, null);
		User suscrito = new User(Delivery.LOCAL, null);
		servicio.addSubscriber(baja);
		servicio.addSubscriber(suscrito);

		servicio.removeSubscriber(baja);

		assertFalse(servicio.getSubscribers().contains(baja));
		assertTrue(servicio.getSubscribers().contains(suscrito));
	}

	@Test
	void rechazaUnaBajaNula() {
		assertThrows(NullUserException.class, () -> servicio.removeSubscriber(null));
	}

	@Test
	void rechazaLaBajaDeUnUsuarioNoSuscrito() {
		User usuario = new User(Delivery.LOCAL, null);

		assertThrows(NonExistingUserException.class, () -> servicio.removeSubscriber(usuario));
	}

	@Test
	void enviarSinSuscriptoresNoDescartaMensajes() {
		assertEquals(0, servicio.sendMessage(new Message(1, "Aviso")));
		verifyNoInteractions(correo);
	}

	@Test
	void reparteElMensajeSegunLaPreferenciaDeCadaSuscriptor() throws Exception {
		User porCorreo = spy(new User(Delivery.EMAIL, "ana@example.com"));
		User local = spy(new User(Delivery.LOCAL, null));
		User sinEntrega = spy(new User(Delivery.DO_NOT_DELIVER, "bea@example.com"));
		User otroSinEntrega = spy(new User(Delivery.DO_NOT_DELIVER, "cris@example.com"));
		servicio.addSubscriber(porCorreo);
		servicio.addSubscriber(local);
		servicio.addSubscriber(sinEntrega);
		servicio.addSubscriber(otroSinEntrega);
		Message mensaje = new Message(2, "Reunión el jueves");

		int descartados = servicio.sendMessage(mensaje);

		assertEquals(2, descartados);
		verify(correo).sendMessage(porCorreo, mensaje);
		verifyNoMoreInteractions(correo);
		verify(porCorreo, never()).saveMessage(any());
		verify(local).saveMessage(mensaje);
		comprobarAvisoDeMensajePerdido(sinEntrega, mensaje);
		comprobarAvisoDeMensajePerdido(otroSinEntrega, mensaje);
	}

	private void comprobarAvisoDeMensajePerdido(User usuario, Message original)
			throws ReflectiveOperationException {
		ArgumentCaptor<Message> aviso = ArgumentCaptor.forClass(Message.class);
		verify(usuario).saveMessage(aviso.capture());
		assertNotSame(original, aviso.getValue());

		// Message no expone el texto mediante su API pública.
		Field texto = Message.class.getDeclaredField("message");
		texto.setAccessible(true);
		assertEquals("Ha perdido ud. un mensaje", texto.get(aviso.getValue()));
	}

}
