
			JMP @start

@add:		// input in #1
			ALLOC 2
			ADD 2 #1
			DEC 1
			TEST 1 #1
			JNZ 9
			RETURN #2
			
@fact:		// input in #1
			ALLOC 2		// allocate 2 registers
			MOV 2,1		// reg2 = 1
@fact_1:	MUL 2,#1	// reg2 *= reg1
			DEC 1		// reg1--
			TEST 1,#1	// check reg1, set flags (zero flag)
			JNZ @fact_1	// jump if not zero
			RETURN #2
			END @fact

@start:		ALLOC 2
			MOV 1,10	// arg to calc
			MOV 2,31337	// 31337 is the id of the new machine
			FORK #2
			LOADCODE #2,@fact
			OUT #2,#1,1
			START #2
			JOIN #2
			IN #2,1,2
			FREE #2
			RETURN #1