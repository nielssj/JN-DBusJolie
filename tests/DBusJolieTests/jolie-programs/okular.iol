type testtype:void {
	.params[1, *]: undefined
}

type returnType:void {
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
		test (testtype) (returnType),
		intm (testtype) (int)
}