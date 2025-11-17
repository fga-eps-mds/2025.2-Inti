## 1. **Test Structure Organization**
```java
// BAD: Mixed concerns
@Test
void testUserCreation() {
    // setup + execution + validation all mixed
}

// GOOD: Arrange-Act-Assert pattern
@Test
void createUser_WithValidData_ShouldReturnUser() {
    // Arrange
    UserRequest request = new UserRequest("John", "john@email.com");
    
    // Act
    UserResponse response = userService.create(request);
    
    // Assert
    assertThat(response.getId()).isNotNull();
    assertThat(response.getName()).isEqualTo("John");
}
```

## 2. **Meaningful Test Naming**
```java
// BAD: Vague names
@Test void test1() {}
@Test void userTest() {}

// GOOD: Descriptive convention
@Test 
void withdrawMoney_WhenInsufficientBalance_ShouldThrowInsufficientFundsException() {}

@Test
void findById_WithNonExistentId_ShouldReturnEmptyOptional() {}
```

## 3. **Isolated Unit Tests**
```java
// BAD: Testing multiple units
@Test
void testOrderProcessing() {
    inventoryService.checkStock();  // Testing inventory
    paymentService.processPayment(); // Testing payment
    shippingService.scheduleDelivery(); // Testing shipping
}

// GOOD: Single responsibility
@Test
void processOrder_WithValidOrder_ShouldUpdateOrderStatus() {
    // Only test order service logic
    Order order = orderService.process(orderRequest);
    assertThat(order.getStatus()).isEqualTo(OrderStatus.PROCESSING);
}
```

## 4. **Mock Dependencies Properly**
```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private EmailService emailService;
    
    @InjectMocks
    private UserService userService;
    
    @Test
    void createUser_ShouldSendWelcomeEmail() {
        // Given
        User user = new User("test@email.com");
        when(userRepository.save(any(User.class))).thenReturn(user);
        
        // When
        userService.createUser(user);
        
        // Then
        verify(emailService).sendWelcomeEmail(user.getEmail());
    }
}
```

**Note:** Never use `@MockBean`, as it is deprecated. Use `@Mock` for unit tests and proper test configurations for integration tests.

## 5. **Test Edge Cases and Boundaries**
```java
@Test
void calculateDiscount_WithVariousAmounts_ShouldApplyCorrectDiscount() {
    assertThat(calculator.calculateDiscount(50.0)).isEqualTo(0.0);   // No discount
    assertThat(calculator.calculateDiscount(100.0)).isEqualTo(5.0);  // 5% discount
    assertThat(calculator.calculateDiscount(500.0)).isEqualTo(50.0); // 10% discount
    assertThat(calculator.calculateDiscount(0.0)).isEqualTo(0.0);    // Zero amount
}

@Test
void parseAge_WithNegativeValue_ShouldThrowException() {
    assertThrows(InvalidAgeException.class, 
        () -> validator.parseAge(-5));
}
```

## 6. **Use Test Data Builders**
```java
// Instead of repetitive object creation
@Test
void testOrder() {
    Order order = new Order();
    order.setId(1L);
    order.setCustomer(new Customer("John", "john@email.com"));
    order.setItems(Arrays.asList(item1, item2, item3));
    // ... more setup
}

// Use builder pattern
@Test
void createOrder_WithMultipleItems_ShouldCalculateTotal() {
    Order order = OrderBuilder.anOrder()
        .withId(1L)
        .withCustomer(CustomerBuilder.aCustomer().withName("John").build())
        .withItems(ItemBuilder.anItem().withPrice(10.0).build())
        .build();
        
    assertThat(order.getTotal()).isEqualTo(30.0);
}
```

## 7. **Parameterized Tests**
```java
@ParameterizedTest
@CsvSource({
    "100, 10, 110",    // principal, interest, expected
    "200, 15, 230",
    "500, 20, 600"
})
void calculateTotalAmount_WithVariousInputs_ShouldReturnCorrectResult(
    double principal, double interest, double expected) {
        
    double result = calculator.calculateTotalAmount(principal, interest);
    assertThat(result).isEqualTo(expected);
}

@ParameterizedTest
@ValueSource(strings = {"", "  ", "\t", "\n"})
void validateName_WithBlankStrings_ShouldReturnFalse(String blankName) {
    assertFalse(validator.isValidName(blankName));
}
```

## 8. **Integration Test Annotations**
```java
@DataJpaTest
class UserRepositoryIntegrationTest {
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private UserRepository userRepository;
    
    @Test
    void findByEmail_WhenUserExists_ShouldReturnUser() {
        // Given
        User user = new User("test@email.com");
        entityManager.persistAndFlush(user);
        
        // When
        Optional<User> found = userRepository.findByEmail("test@email.com");
        
        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("test@email.com");
    }
}

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ExternalServiceIntegrationTest {
    // Tests with real database
}
```

## 9. **Test Exception Handling**
```java
@Test
void transferMoney_WhenFromAccountNotFound_ShouldThrowAccountNotFoundException() {
    // Given
    TransferRequest request = new TransferRequest(999L, 2L, 100.0);
    when(accountRepository.findById(999L)).thenReturn(Optional.empty());
    
    // When & Then
    assertThrows(AccountNotFoundException.class, 
        () -> transferService.transferMoney(request));
}

@Test
void divide_ByZero_ShouldThrowArithmeticException() {
    Calculator calculator = new Calculator();
    
    Exception exception = assertThrows(ArithmeticException.class, 
        () -> calculator.divide(10, 0));
        
    assertThat(exception.getMessage()).contains("divide by zero");
}
```

## 10. **Verify Method Interactions**
```java
@Test
void updateUserProfile_ShouldUpdateAndLogAction() {
    // Given
    User user = new User(1L, "old@email.com");
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    
    // When
    userService.updateEmail(1L, "new@email.com");
    
    // Then
    verify(userRepository).save(user);
    verify(auditLogger).logUserUpdate(1L, "Email updated");
    verify(emailService, never()).sendNotification(any()); // Ensure not called
}
```

## 11. **Async Testing**
```java
@Test
void processOrderAsync_ShouldCompleteWithinTimeout() {
    // Given
    Order order = new Order(1L);
    
    // When
    CompletableFuture<Order> future = orderService.processAsync(order);
    
    // Then
    assertThat(future)
        .succeedsWithin(5, TimeUnit.SECONDS)
        .extracting(Order::getStatus)
        .isEqualTo(OrderStatus.COMPLETED);
}

@Test
void sendNotification_ShouldReturnCompletedFuture() {
    Notification notification = new Notification("Test message");
    
    CompletableFuture<Void> result = notificationService.send(notification);
    
    assertThat(result.isDone()).isTrue();
    assertThat(result.isCompletedExceptionally()).isFalse();
}
```

## 12. **Database Rollback Tests**
```java
@SpringBootTest
@Transactional
class UserServiceIntegrationTest {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Test
    void createUser_WithUniqueEmail_ShouldPersistUser() {
        // Given
        UserRequest request = new UserRequest("John", "unique@email.com");
        
        // When
        UserResponse response = userService.createUser(request);
        
        // Then - This will be rolled back after test
        assertThat(userRepository.findByEmail("unique@email.com"))
            .isPresent()
            .get()
            .extracting(User::getName)
            .isEqualTo("John");
    }
}
```

## 13. **Test Configuration Management**
```java
@TestConfiguration
static class TestConfig {
    @Bean
    @Primary
    public PaymentService mockPaymentService() {
        return mock(PaymentService.class);
    }
}

@SpringBootTest
@Import(TestConfig.class)
class OrderServiceTest {
    
    @Autowired
    private PaymentService paymentService;
    
    @Test
    void createOrder_ShouldProcessPayment() {
        when(paymentService.process(any())).thenReturn(new PaymentResult(Status.SUCCESS));
        
        orderService.createOrder(orderRequest);
        
        verify(paymentService).process(any(PaymentRequest.class));
    }
}
```

## 14. **JSON Assertion Testing**
```java
@Test
void getUser_ShouldReturnCorrectJson() throws Exception {
    // Given
    when(userService.findById(1L)).thenReturn(new User(1L, "John", "john@email.com"));
    
    // When & Then
    mockMvc.perform(get("/api/users/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.name").value("John"))
        .andExpect(jsonPath("$.email").value("john@email.com"))
        .andExpect(jsonPath("$.links[0].rel").value("self"));
}

@Test
void createUser_WithInvalidJson_ShouldReturnBadRequest() throws Exception {
    String invalidJson = "{\"name\": \"John\", \"email\": \"invalid-email\"}";
    
    mockMvc.perform(post("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(invalidJson))
        .andExpect(status().isBadRequest());
}
```

## 15. **Performance and Load Testing**
```java
@Test
void processBatch_WithThousandItems_ShouldCompleteUnderOneSecond() {
    // Given
    List<Order> orders = IntStream.range(0, 1000)
        .mapToObj(i -> new Order(i, 100.0))
        .collect(Collectors.toList());
    
    // When
    long startTime = System.currentTimeMillis();
    batchService.processOrders(orders);
    long endTime = System.currentTimeMillis();
    
    // Then
    long duration = endTime - startTime;
    assertThat(duration).isLessThan(1000); // 1 second
}

@RepeatedTest(10)
void databaseQuery_ShouldBeConsistentlyFast() {
    long startTime = System.nanoTime();
    
    List<User> users = userRepository.findActiveUsers();
    
    long duration = System.nanoTime() - startTime;
    assertThat(duration).isLessThan(100_000_000); // 100ms in nanoseconds
}
```

## Key Objectives for Copilot:
1. **Maintainability** - Write clean, readable tests
2. **Reliability** - Ensure consistent test results
3. **Coverage** - Test both happy paths and edge cases
4. **Performance** - Write efficient tests
5. **Isolation** - Keep tests independent
6. **Documentation** - Use tests as documentation
7. **CI/CD Ready** - Fast, reliable tests for pipelines

These instructions will help Copilot generate comprehensive, maintainable tests that follow best practices and provide good coverage for your codebase.