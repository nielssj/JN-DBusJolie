type ListOfNames:void {
	.params[1,*]:undefined
}

interface DBusInterface {
	RequestResponse: 
	  ListNames( void ) ( ListOfNames )
}
