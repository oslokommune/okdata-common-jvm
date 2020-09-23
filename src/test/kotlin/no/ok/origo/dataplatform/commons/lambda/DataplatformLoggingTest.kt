package no.ok.origo.dataplatform.commons.lambda

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.kotlintest.matchers.collections.shouldContain
import io.kotlintest.matchers.collections.shouldContainAll
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.kotlintest.specs.AnnotationSpec
import java.io.ByteArrayOutputStream
import uk.org.lidalia.slf4jtest.TestLogger
import uk.org.lidalia.slf4jtest.TestLoggerFactory

internal class DataplatformLoggingTest() : AnnotationSpec() {

    val testContext = TestContext()

    lateinit var handler: DataplatformHandler
    lateinit var logger: TestLogger
    val om = jacksonObjectMapper()

    @BeforeEach
    fun beforeEach() {
        logger = TestLoggerFactory.getTestLogger(DataplatformHandler::class.java)
        handler = DataplatformHandler()
    }

    @AfterEach
    fun afterEach() {
        logger.clearAll()
    }

    @Test
    fun `normal methods should not log`() {
        handler.final(testContext)
        logger.loggingEvents.size shouldBe 0
    }

    @Test
    fun `log only when handleRequest completes`() {
        val out = ByteArrayOutputStream()
        handler.handleRequest("".byteInputStream(), out, testContext)
        logger.loggingEvents.size shouldBe 1
        val statements: List<Pair<String, String>> = om.readValue(logger.allLoggingEvents.single().message)
        statements shouldContain Pair("final", "hello again")
        statements shouldContain Pair("user", "ID-123")
    }

    @Test
    fun `add context information to log statement`() {
        val out = ByteArrayOutputStream()
        handler.handleRequest("".byteInputStream(), out, testContext)
        logger.loggingEvents.size shouldBe 1
        val statements: List<Pair<String, String>> = om.readValue(logger.allLoggingEvents.single().message)
        statements.toMap().keys shouldContainAll listOf(
                "aws_request_id",
                "function_name",
                "memory_limit_in_mb",
                "remaining_time_in_millis",
                "service_name"
        )
        statements shouldContain Pair("function_name", testContext.functionName)
        statements shouldContain Pair("service_name", System.getenv("SERVICE_NAME"))
    }

    @Test
    fun `log exceptions`() {
        logger = TestLoggerFactory.getTestLogger(DataplatformHandlerThrowsException::class.java)
        val out = ByteArrayOutputStream()
        val throwsException = DataplatformHandlerThrowsException()
        shouldThrow<ExpectedException> {
            throwsException.handleRequest("".byteInputStream(), out, testContext)
        }
        logger.loggingEvents.size shouldBe 1
        val statements: List<Pair<String, String>> = om.readValue(logger.allLoggingEvents.single().message)
        val expectedException = ExpectedException()
        statements shouldContain Pair("exception", expectedException.message)
        statements shouldContain Pair("exception_name", "ExpectedException")
    }
}
