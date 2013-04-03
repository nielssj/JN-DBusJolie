interface Okular {
	OneWay: 
		slotNextPage( void ),
		goToPage( int ),
		openDocument( string )
	RequestResponse: 
		currentPage ( void ) ( int ),
		currentDocument ( void ) ( string )
}

interface Okular__shell {
	RequestResponse: 
		close ( void )( bool )
}