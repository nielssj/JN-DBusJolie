type testtype:void {
	.params[1, *]: undefined
}

type returnType:void {
	.params[1, *]: undefined
}


interface Okular {
	OneWay: 
		goToPage( long ),
		openDocument( string ),
		test(testtype)
	RequestResponse: 
		slotNextPage( void ) ( void ),
		
		currentPage ( void ) ( long ),
		currentDocument ( void ) ( string )
}