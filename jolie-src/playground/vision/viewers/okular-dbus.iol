interface OkularDBusInterface {
	OneWay:
	  goToPage( long ),
	  openDocument( string )
	RequestResponse: 
	  currentPage( void ) ( long ),
	  currentDocument( void ) ( string )
}
