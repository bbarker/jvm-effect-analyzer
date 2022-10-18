package io.github.bbarker.eff.analyzerhealthcheck

import zio.test.Assertion.*
import zio.test.*

object SimpleSpec extends ZIOSpecDefault:
  def spec = suite("http")(
    suite("simple check")(
      test("???") {
        assertTrue(1 + 1 == 2)
      }
    )
  )
