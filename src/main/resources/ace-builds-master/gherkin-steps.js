// Steps that can be used for gherkin
var simplePotentials = [
"Given I have a 6502 machine",
"When I assemble this file *"
];

// Note: The indentation for snippets must be tabs not spaces!
var complexPotentials = "\
snippet Then the memory at address is value\n\
	Then the memory at ${1:address} is ${2:value}\n\
";
