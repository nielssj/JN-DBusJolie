type testType:void {
	.intValue: undefined
	.longMapArray[1, 2]: void {
		.long1: undefined
		.long2: undefined
	}
	.vArray[1, 2]: undefined
}

interface Variant {
	RequestResponse:
		testVariant ( testType )( testType )
}