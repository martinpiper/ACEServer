Feature: t1
  Scenario: t1
    Given I have a 6502 machine
    When I assemble this file *
    Then the memory at address is value