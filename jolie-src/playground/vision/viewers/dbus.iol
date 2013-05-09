type ListOfNames:void {
	.arg0[1,*]: string
}

interface DBusInterface {
	RequestResponse: 
	  ListNames( void ) ( ListOfNames )
}
