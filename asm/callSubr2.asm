@fact:		// input in #1
			ALLOC 2		// allocate 2 registers
			MOV 2,1		// reg2 = 1
@fact_1:	MUL 2,#1	// reg2 *= reg1
			DEC 1		// reg1--
			TEST 1,#1	// check reg1, set flags (zero flag)
			JNZ @fact_1	// jump if not zero
			RETURN #2