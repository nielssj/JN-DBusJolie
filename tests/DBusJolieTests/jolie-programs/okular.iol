type testtype:void {
	.field1 [1 ,2 ]: string
	.field2: string
	.field3: void {
		.values [1, 2]:int
	}
}

interface Okular {
	OneWay: 
		slotNextPage( void ),
		goToPage( int ),
		openDocument( string ),
		testmethod ( testtype ) 
	RequestResponse: 
		currentPage ( void ) ( int ),
		currentDocument ( void ) ( string )

}

interface Okular__shell {
	RequestResponse: 
		close ( void )( bool )
}