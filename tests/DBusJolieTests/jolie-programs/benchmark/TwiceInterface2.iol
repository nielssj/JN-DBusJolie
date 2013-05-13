type LargeTwiceRequest: void {
	  .values [1, *]: int
}

interface LargeTwiceInterface {
	RequestResponse: 
	  twice( LargeTwiceRequest ) ( int )
}
