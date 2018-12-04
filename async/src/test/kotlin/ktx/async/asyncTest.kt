package ktx.async

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.GdxRuntimeException
import com.badlogic.gdx.utils.async.AsyncExecutor
import com.nhaarman.mockito_kotlin.*
import io.kotlintest.matchers.shouldThrow
import kotlinx.coroutines.GlobalScope
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.concurrent.CompletableFuture

class AsyncTest {
  @Before
  fun `initiate LibGDX application`() {
    Gdx.app = mock()
  }

  @Test
  fun `should prohibit accessing rendering thread and executor before initiation`() {
    shouldThrow<UninitializedPropertyAccessException> { KtxAsync.mainThread }
    shouldThrow<UninitializedPropertyAccessException> { KtxAsync.asyncExecutor }
    shouldThrow<UninitializedPropertyAccessException> {
      GlobalScope.ktxAsync {
        fail("Should prohibit executing coroutines without context.")
      }
    }
  }

  @Test
  fun `should initiate coroutines context`() {
    KtxAsync.initiate()

    assertSame(Thread.currentThread(), KtxAsync.mainThread)
  }

  @Test
  fun `should create AsyncExecutor`() {
    KtxAsync.createExecutor(concurrencyLevel = 1)

    assertNotNull(KtxAsync.asyncExecutor)
  }

  @Test
  fun `should enable coroutines`() {
    enableKtxCoroutines()

    assertSame(Thread.currentThread(), KtxAsync.mainThread)
    shouldThrow<UninitializedPropertyAccessException> { KtxAsync.asyncExecutor }
  }

  @Test
  fun `should enable coroutines with AsyncExecutor given concurrency level`() {
    enableKtxCoroutines(asynchronousExecutorConcurrencyLevel = 1)

    assertSame(Thread.currentThread(), KtxAsync.mainThread)
    assertNotNull(KtxAsync.asyncExecutor)
  }

  @Test
  fun `should report if on thread that initiated the context`() {
    KtxAsync.initiate()

    assertTrue(KtxAsync.isOnRenderingThread())
  }

  @Test
  fun `should report if not on thread that initiated the context`() {
    CompletableFuture.runAsync { KtxAsync.initiate() }.join()

    assertNotNull(KtxAsync.mainThread)
    assertNotSame(Thread.currentThread(), KtxAsync.mainThread)
    assertFalse(KtxAsync.isOnRenderingThread())
  }

  @Test
  fun `should not add thread verification task if validation is turned off`() {
    enableKtxCoroutines(validate = false)

    verifyZeroInteractions(Gdx.app)
  }

  @Test
  fun `should add thread verification task`() {
    val tasks = mutableListOf<Runnable>()
    Gdx.app = mock {
      on(it.postRunnable(any())) doAnswer { tasks.add(it.getArgument(0)); Unit }
    }

    CompletableFuture.runAsync { enableKtxCoroutines() }.join()

    assertNotNull(KtxAsync.mainThread)
    assertNotSame(Thread.currentThread(), KtxAsync.mainThread)
    assertEquals(1, tasks.size)
    shouldThrow<GdxRuntimeException> { tasks.forEach(Runnable::run) }
  }

  @Test
  fun `should pass delay tasks to main LibGDX Timer`() = `with timer`(onSchedule = `no delay execution`()) { timer ->
    enableKtxCoroutines()

    GlobalScope.ktxAsync {
      delay(1.5f)
    }

    verify(timer).scheduleTask(any(), eq(1.5f))
  }

  @Test
  fun `should suspend coroutine and resume on next frame`() = `coroutine test` { ktxAsync ->
    val app = Gdx.app as TestApplication
    ktxAsync {
      val frameId = app.frameId
      skipFrame()
      val nextFrameId = app.frameId

      assertNotEquals(frameId, nextFrameId)
      assertEquals(frameId, nextFrameId - 1)
    }
  }

  @Test
  fun `should cancel frame skip`() = `cancelled coroutine test`(cancelAfterMillis = 0L) { ktxAsync, _ ->
    ktxAsync {
      skipFrame()
      fail("Should be cancelled.")
    }
  }

  @Test
  fun `should delay coroutine execution`() = `with timer` { timer ->
    `coroutine test` { ktxAsync ->
      val beforeExecution = System.currentTimeMillis()
      ktxAsync {
        delay(0.1f)

        val executionTime = System.currentTimeMillis() - beforeExecution
        verify(timer).scheduleTask(any(), eq(0.1f))
        // Mocks, test runner, scheduler and coroutines overhead might affect execution time, but if the delay is longer
        // than 0.05s, something might be is wrong.
        assertTrue("100 milliseconds delay execution took $executionTime milliseconds.", executionTime in 100L..150L)
      }
    }
  }

  @Test
  fun `should not delay coroutine execution given nonexistent delay`() = `with timer` { timer ->
    `coroutine test` { ktxAsync ->
      ktxAsync {
        delay(0f)

        // Should resume immediately without invoking the timer:
        verify(timer, never()).scheduleTask(any(), any())
      }
    }
  }

  @Test
  fun `should cancel delay task`() = `with timer` {
    `cancelled coroutine test` { ktxAsync, _ ->
      ktxAsync {
        delay(0.05f)
        fail("Should be cancelled.")
      }
    }
  }

  @Test
  fun `should perform asynchronous action`() = `coroutine test`(concurrencyLevel = 1) { ktxAsync ->
    ktxAsync {
      val (result, thread) = asynchronous { "Test." to Thread.currentThread() }

      assertEquals("Test.", result)
      assertNotSame(KtxAsync.mainThread, thread)
    }
  }

  @Test
  fun `should perform asynchronous action with a custom executor`() = `coroutine test` { ktxAsync ->
    KtxAsync.asyncExecutor = mock()
    val executor = AsyncExecutor(1)

    ktxAsync {
      val (result, thread) = asynchronous(executor) { "Test." to Thread.currentThread() }

      verifyZeroInteractions(KtxAsync.asyncExecutor)
      assertEquals("Test.", result)
      assertNotSame(KtxAsync.mainThread, thread)
    }
  }

  @Test
  fun `should handle asynchronous action exceptions`() = `coroutine test`(concurrencyLevel = 1) { ktxAsync ->
    ktxAsync {
      shouldThrow<GdxRuntimeException> {
        asynchronous { throw GdxRuntimeException("Expected.") }
      }
    }
  }

  @Test
  fun `should cancel asynchronous actions`() = `cancelled coroutine test`(concurrencyLevel = 1) { ktxAsync, _ ->
    ktxAsync {
      asynchronous {
        Thread.sleep(50L) // Making sure the test runner is able to cancel the task before it is finished.
        "test"
      }
      fail("Should cancel asynchronous action.")
    }
  }

  @After
  fun `clear context`() {
    Gdx.app = null
    `destroy coroutines context`()
  }
}
