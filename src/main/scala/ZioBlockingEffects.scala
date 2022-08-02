package tc.lab.daniel
import utils.*

import zio.*

import java.util.concurrent.atomic.AtomicBoolean

object ZioBlockingEffects extends ZIOAppDefault:

    def blockingTask(n: Int): UIO[Unit] =
      ZIO.succeed(s"running blocking task $n").debugThread *>
        ZIO.succeed(Thread.sleep(8000)) *> blockingTask(n)

    val program = ZIO.foreachPar((1 to 100).toList)(blockingTask)

    val aBlockingZIO = ZIO.attemptBlocking {
      println(
        s"[${Thread.currentThread().getName}] running a long computation..."
      )
      Thread.sleep(10000)
      42
    }

    val tryInterrupting =
      for
          blockingFiber <- aBlockingZIO.fork
          _ <- Clock.sleep(1.second) *> ZIO
            .succeed(
              "interrupting..."
            )
            .debugThread *> blockingFiber.interrupt
          mol <- blockingFiber.join
      yield mol

    val aBlockingInterruptibleZIO = ZIO.attemptBlockingInterrupt {
      println(
        s"[${Thread.currentThread().getName}] running a long computation..."
      )
      Thread.sleep(10000)
      42
    }

    // set a flag/switch
    def interruptibleBlockingEffect(canceledFlag: AtomicBoolean): Task[Unit] =
      ZIO.attemptBlockingCancelable {
        (1 to 10000).foreach { element =>
          if !canceledFlag.get() then {
            println(element)
            Thread.sleep(100)
          }
        }
      }(ZIO.succeed(canceledFlag.set(true)))

    val interruptibleBlockingDemo =
      for
          fiber <- interruptibleBlockingEffect(new AtomicBoolean(false)).fork
          _ <- Clock
            .sleep(2.seconds) *> ZIO
            .succeed("interrupting...")
            .debugThread *> fiber.interrupt
          _ <- fiber.join
      yield ()

    def run = interruptibleBlockingDemo
