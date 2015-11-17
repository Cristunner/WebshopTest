package se.grouprich.webshop.service;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import se.grouprich.webshop.exception.CustomerRegistrationException;
import se.grouprich.webshop.exception.OrderException;
import se.grouprich.webshop.exception.PaymentException;
import se.grouprich.webshop.exception.ProductRegistrationException;
import se.grouprich.webshop.idgenerator.IdGenerator;
import se.grouprich.webshop.model.Customer;
import se.grouprich.webshop.model.Order;
import se.grouprich.webshop.model.Product;
import se.grouprich.webshop.model.ShoppingCart;
import se.grouprich.webshop.repository.Repository;
import se.grouprich.webshop.service.validation.CustomerValidator;
import se.grouprich.webshop.service.validation.PasswordValidator;

@RunWith(MockitoJUnitRunner.class)
public class ECommerceServiceTest
{
	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Mock(name = "orderRepository")
	private Repository<String, Order> orderRepositoryMock;
	@Mock(name = "customerRepository")
	private Repository<String, Customer> customerRepositoryMock;
	@Mock(name = "productRepository")
	private Repository<String, Product> productRepositoryMock;
	private ECommerceService eCommerceService;
	@Mock
	private IdGenerator<String> idGeneratorMock;
	private PasswordValidator eCommerceValidator = new CustomerValidator();
	private String password = "secret";
	private String firstName = "Haydee";
	private String lastName = "Arbeito";
	private String orderId = "1002";
	
//	@InjectMocks <-- funkar inte. Debuggade. Den fattar inte vilken Repository är orderRepository. Det verkar som att den tar den första Mocken i alfabetisk ordning.
//	private ECommerceService eCommerceService;

	@Before
	public void setup()
	{
		eCommerceService = new ECommerceService(orderRepositoryMock, customerRepositoryMock, productRepositoryMock, idGeneratorMock, eCommerceValidator);
	}

	@Test
	public void customerShouldNotHaveEmailAddressThatIsLongerThan30Characters() throws CustomerRegistrationException
	{
		exception.expect(CustomerRegistrationException.class);
		exception.expectMessage(equalTo("Email address that is longer than 30 characters is not allowed"));

		String email = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa@aa.com";

		eCommerceService.createCustomer(email, password, firstName, lastName);

		assertTrue(email.length() > 30);
	}

	@Test
	public void orderThatLacksOrderRowsShouldNotBeAccepted() throws CustomerRegistrationException, PaymentException, OrderException
	{
		exception.expect(OrderException.class);
		exception.expectMessage(equalTo("Shopping cart is empty"));
		
		Customer customer = new Customer(null, null, null, null, null);
		ShoppingCart shoppingCart = new ShoppingCart();
		
		eCommerceService.checkOut(customer, shoppingCart);

		assertTrue(shoppingCart.getProducts().isEmpty());
	}

	@Test
	public void orderShouldHaveGotItsIdAssigned() throws CustomerRegistrationException, PaymentException, OrderException
	{		
		Customer customer = new Customer(null, null, null, null, null);
		ShoppingCart shoppingCart = new ShoppingCart();
		Order order = new Order(orderId, customer, shoppingCart);
		when(idGeneratorMock.getGeneratedId()).thenReturn(orderId);
		
		eCommerceService.createOrder(order);
		
		assertEquals(orderId, order.getId());

		verify(idGeneratorMock).getGeneratedId();
	}
	
//	fixat denna metod efter ändringar
	@Test
	public void totalPriceShouldNotExceedSEK50000() throws ProductRegistrationException, OrderException, PaymentException, CustomerRegistrationException
	{
		exception.expect(PaymentException.class);
		exception.expectMessage(equalTo("We can not accept the total price exceeding SEK 50,000"));

		ShoppingCart shoppingCart = new ShoppingCart();
		shoppingCart.setTotalPrice(50001.00);
		Order order = new Order(null, null, shoppingCart);
		
		eCommerceService.createOrder(order);
		
		assertTrue(shoppingCart.getTotalPrice() > 50000);
	}
}