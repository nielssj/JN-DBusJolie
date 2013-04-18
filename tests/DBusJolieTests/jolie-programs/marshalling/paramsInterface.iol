type testType:void {
	.intValue: int
	.stringValue: string
	.intArray[1, 2]: int
}

interface Params {
	RequestResponse: 
		testParams ( testType )( testType )
}