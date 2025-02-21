```diff
  1507:  private int nextNonWhitespace(boolean throwOnEof) throws IOException {
  1508:    /*
  1509:     * This code uses ugly local variables 'p' and 'l' representing the 'pos'
  1510:     * and 'limit' fields respectively. Using locals rather than fields saves
  1511:     * a few field reads for each whitespace character in a pretty-printed
  1512:     * document, resulting in a 5% speedup. We need to flush 'p' to its field
  1513:     * before any (potentially indirect) call to fillBuffer() and reread both
  1514:     * 'p' and 'l' after any (potentially indirect) call to the same method.
  1515:     */
  1516:    // +1
+ 1517:    Coverage.sample();
  1518:    char[] buffer = this.buffer;
  1519:    int p = pos;
  1520:    int l = limit;
  1521:    while (true) {
  1522:      // +1
+ 1523:      Coverage.sample();
  1524:      if (p == l) {
  1525:        // +1
+ 1526:        Coverage.sample();
  1527:        pos = p;
  1528:        if (!fillBuffer(1)) {
  1529:          // +1
+ 1530:          Coverage.sample();
  1531:          break;
  1532:        } else {
+ 1533:          Coverage.sample();
  1534:        }
  1535:        p = pos;
  1536:        l = limit;
  1537:      } else {
+ 1538:        Coverage.sample();
  1539:      }
  1540:
  1541:      int c = buffer[p++];
  1542:      if (c == '\n') {
  1543:        // +1
+ 1544:        Coverage.sample();
  1545:        lineNumber++;
  1546:        lineStart = p;
  1547:        continue;
  1548:      } else if (c == ' ' || c == '\r' || c == '\t') {
  1549:        // +1
+ 1550:        Coverage.sample();
  1551:        continue;
  1552:      } else {
+ 1553:        Coverage.sample();
  1554:      }
  1555:
  1556:      if (c == '/') {
  1557:        // +1
+ 1558:        Coverage.sample();
  1559:        pos = p;
  1560:        if (p == l) {
  1561:          // +1
+ 1562:          Coverage.sample();
  1563:          pos--; // push back '/' so it's still in the buffer when this method returns
  1564:          boolean charsLoaded = fillBuffer(2);
  1565:          pos++; // consume the '/' again
  1566:          if (!charsLoaded) {
  1567:            // +1
+ 1568:            Coverage.sample();
  1569:            return c;
  1570:          } else {
- 1571:            Coverage.sample();
  1572:          }
  1573:        } else {
+ 1574:          Coverage.sample();
  1575:        }
  1576:
  1577:        checkLenient();
  1578:        char peek = buffer[pos];
  1579:        switch (peek) {
  1580:          case '*':
  1581:            // skip a /* c-style comment */
  1582:            // +1
+ 1583:            Coverage.sample();
  1584:            pos++;
  1585:            if (!skipTo("*/")) {
  1586:              // +1
- 1587:              Coverage.sample();
  1588:              throw syntaxError("Unterminated comment");
  1589:            } else {
+ 1590:              Coverage.sample();
  1591:            }
  1592:            p = pos + 2;
  1593:            l = limit;
  1594:            continue;
  1595:
  1596:          case '/':
  1597:            // skip a // end-of-line comment
  1598:            // +1
+ 1599:            Coverage.sample();
  1600:            pos++;
  1601:            skipToEndOfLine();
  1602:            p = pos;
  1603:            l = limit;
  1604:            continue;
  1605:
  1606:          default:
  1607:            // +1
+ 1608:            Coverage.sample();
  1609:            return c;
  1610:        }
  1611:      } else if (c == '#') {
  1612:        // +1
+ 1613:        Coverage.sample();
  1614:        pos = p;
  1615:        /*
  1616:         * Skip a # hash end-of-line comment. The JSON RFC doesn't
  1617:         * specify this behaviour, but it's required to parse
  1618:         * existing documents. See http://b/2571423.
  1619:         */
  1620:        checkLenient();
  1621:        skipToEndOfLine();
  1622:        p = pos;
  1623:        l = limit;
  1624:      } else {
+ 1625:        Coverage.sample();
  1626:        pos = p;
  1627:        return c;
  1628:      }
  1629:    }
  1630:    if (throwOnEof) {
  1631:      // +1
+ 1632:      Coverage.sample();
  1633:      throw new EOFException("End of input" + locationString());
  1634:    } else {
+ 1635:      Coverage.sample();
  1636:      return -1;
  1637:    }
  1638:  }


```
