// Steps that can be used for gherkin
var simplePotentials = [
"I have a 6502 machine",
"Given I have a 6502 machine",
"I assemble this file *",
"When I assemble this file *"
];

// Note: The indentation for snippets must be tabs not spaces!
var complexPotentials = "\
snippet the memory at address is value\n\
	the memory at ${1:address} is ${2:value}\n\
";
