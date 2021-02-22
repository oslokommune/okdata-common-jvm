package no.ok.origo.dataplatform.commons.lambda

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.kotlintest.matchers.collections.shouldContainAll
import io.kotlintest.matchers.collections.shouldNotContain
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.kotlintest.specs.AnnotationSpec
import io.mockk.every
import io.mockk.mockkStatic
import uk.org.lidalia.slf4jtest.TestLogger
import uk.org.lidalia.slf4jtest.TestLoggerFactory
import java.io.ByteArrayOutputStream
import java.time.ZonedDateTime

internal class DataplatformLoggingRequestHandlerTest : AnnotationSpec() {

    val testContext = TestContext("aws-request-id-1234")

    lateinit var handler: DataplatformRequestHandler
    lateinit var logger: TestLogger
    val om = jacksonObjectMapper()

    @BeforeEach
    fun beforeEach() {
        logger = TestLoggerFactory.getTestLogger(DataplatformRequestHandler::class.java)
        handler = DataplatformRequestHandler()
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
        handler.handleRequest(LambdaEvent("foo", "bar"), testContext)
        logger.loggingEvents.size shouldBe 1
        val statements: Map<String, Any> = om.readValue(logger.allLoggingEvents.single().message)
        statements.get("final") shouldBe "hello again"
        statements.get("user") shouldBe "ID-123"
        statements.get("level") shouldBe "info"
    }

    @Test
    fun `add context information to log statement`() {
        handler.handleRequest(LambdaEvent("foo", "bar"), testContext)
        logger.loggingEvents.size shouldBe 1
        val statements: Map<String, Any> = om.readValue(logger.allLoggingEvents.single().message)
        statements.keys shouldContainAll listOf(
            "aws_request_id",
            "function_name",
            "memory_limit_in_mb",
            "remaining_time_in_millis",
            "service_name"
        )
        statements.get("function_name") shouldBe testContext.functionName
        statements.get("service_name") shouldBe System.getenv("SERVICE_NAME")
    }

    @Test
    fun `log content gets cleared for each call`() {
        val extraLogEntriesFirstRequest = listOf(Pair("kake", "vaffel"), Pair("length", 2))
        handler.logStuffAndHandleRequest(testContext, extraLogEntriesFirstRequest)
        logger.loggingEvents.size shouldBe 1
        val statements: Map<String, Any> = om.readValue(logger.allLoggingEvents.single().message)
        statements.get("aws_request_id") shouldBe testContext.awsRequestId
        statements.get("kake") shouldBe "vaffel"
        statements.get("length") shouldBe 2

        val secondTestContext = TestContext("aws-request-id-4321")
        handler.logStuffAndHandleRequest(secondTestContext, emptyList())
        val secondStatements: Map<String, Any> = om.readValue(logger.allLoggingEvents.get(1).message)
        secondStatements.get("aws_request_id") shouldBe secondTestContext.awsRequestId
        secondStatements.keys shouldNotContain "kake"
        secondStatements.keys shouldNotContain "length"
    }

    @Test
    fun `timestamp has correct format`() {
        val localDatetimeExpected = ZonedDateTime.parse("2021-02-17T11:11:49.249+01:00")
        mockkStatic(ZonedDateTime::class)
        every { ZonedDateTime.now() } returns localDatetimeExpected
        handler.handleRequest(LambdaEvent("foo", "bar"), testContext)
        logger.loggingEvents.size shouldBe 1
        val statements: Map<String, Any> = om.readValue(logger.allLoggingEvents.single().message)
        statements.get("timestamp") shouldBe "2021-02-17T10:11:49.249Z"
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
        val statements: Map<String, Any> = om.readValue(logger.allLoggingEvents.single().message)
        val expectedException = ExpectedException()
        statements.get("level") shouldBe "error"
        statements.get("exception") shouldBe expectedException.message
        statements.get("exception_name") shouldBe "ExpectedException"
        val stacktrace = statements.get("stacktrace") as String
        stacktrace.startsWith("no.ok.origo.dataplatform.commons.lambda.ExpectedException: Expected exception in test") shouldBe true
    }
}
