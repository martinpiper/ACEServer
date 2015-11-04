Feature: t1
  Scenario: t1
    Given passed
    Then failed
    Given undefined
    Then pending
    Given I have a 6502 machine

    Then passed
    Given I have a 6502 machine
    Then the memory at 0x400 is 0x01
    Given pending
    Then the memory at 0x400 is 0x01
    Given I have a 6502 machine
    Then undefined
    Then foo
    Then foo
    Then skipped