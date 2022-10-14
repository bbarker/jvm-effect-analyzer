package io.github.bbarker.eff.analyzerhealthcheck

import zio.test.*
import zio.test.Assertion.*

object SimpleSpec extends ZIOSpecDefault:
  def spec = suite("http")(
    suite("simple check")(
      test("???") {
        assertTrue(1+1 == 2)
      }
    )
  )