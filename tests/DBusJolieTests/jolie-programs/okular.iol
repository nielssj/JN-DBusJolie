type testtype:void {
	.params[1, *]: undefined
}

type returnType:void {
	.params[1, *]: undefined
}


interface Okular {
	OneWay: 
		slotNextPage( void ),
		goToPage( int ),
		openDocument( string ),
		testmethod (int),
		testmethod2 ( testtype ) 
	RequestResponse: 
		currentPage ( void ) ( int ),
		currentDocument ( void ) ( string )

}

interface Okular__shell {
	RequestResponse: 
		close ( void )( bool )
}

interface HelloServer {
	RequestResponse:
		test (int) (returnType),
		intm (testtype) (int)
}