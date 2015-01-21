package com.adviser.dhcponzoo

import java.io.FileReader
import org.junit.Test

import static org.junit.Assert.*
import static org.hamcrest.Matchers.*

/**
 * @author markusw
 */
class LeaseParserTest {

  @Test
  def testParseFile() {
    val reader = new FileReader('src/test/resources/test.lease');
    try {
      val map = new LeaseParser().parse(reader)

      assertThat(map, hasKey('10.1.222.51'))
      assertThat(map, hasKey('10.1.222.52'))
      assertThat(map.get('10.1.222.51'), hasKey('starts'))
      assertThat(map.get('10.1.222.51').get('starts'), is('3 2015/01/21 07:34:38'))
    } finally {
      reader.close()
    }
  }

}
