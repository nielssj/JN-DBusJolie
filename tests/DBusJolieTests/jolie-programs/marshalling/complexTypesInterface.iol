type testType:void {
	.intValue: int
	.stringValue: string
	.boolMap: void {
		.trueValue: bool
		.falseValue: bool
	}
	.longMapArray[1, 2]: void {
		.long1: long
		.long2: long
	}
	.intArray[1, 2]: int
}

interface Complex {
	OneWay:
		onewaymethod (int)
	RequestResponse:
		testParams ( testType )( testType )
}
