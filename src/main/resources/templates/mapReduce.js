db..mapReduce(
		function() { emit( this.f1,this.f2 ) },
		function(key, values) { return Array.sum( values ) },
		{
			query: { status: "A" },
			out:   {  inline: 1 }
		}
)
