package no.ok.origo.dataplatform.commons.logging

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

    lateinit var handler: DataplatformLoggingHandler
    lateinit var logger: TestLogger
    val om = jacksonObjectMapper()

    @BeforeEach
    fun beforeEach() {
        logger = TestLoggerFactory.getTestLogger(testContext.awsRequestId)
        handler = DataplatformLoggingHandler()
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
                "awsRequestId",
                "functionName",
                "memoryLimitInMB",
                "remainingTimeInMillis"
        )
        statements shouldContain Pair("functionName", testContext.functionName)
    }

    @Test
    fun `log exceptions`() {
        val out = ByteArrayOutputStream()
        val throwsException = DataplatformLoggingHandlerThrowsException()
        shouldThrow<ExpectedException> {
            throwsException.handleRequest("".byteInputStream(), out, testContext)
        }
        logger.loggingEvents.size shouldBe 1
        val statements: List<Pair<String, String>> = om.readValue(logger.allLoggingEvents.single().message)
        val expectedException = ExpectedException()
        statements shouldContain Pair("Exception", expectedException.message)
        statements shouldContain Pair("ExceptionName", "ExpectedException")
    }
}
