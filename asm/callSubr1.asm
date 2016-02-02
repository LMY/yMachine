
			JMP @start

@add:		// input in #1
			ALLOC 2
			ADD 2 #1
			DEC 1
			TEST 1 #1
			JNZ 9
			RETURN #2
			
@fact:		INCLUDE asm/callSubr2.asm		

@start:		ALLOC 2
			MOV 1,10	// arg to calc
			MOV 2,31337	// 31337 is the id of the new machine
			FORK #2
			LOADCODE #2,@fact,100
			OUT #2,#1,1
			START #2
			JOIN #2
			IN #2,1,2
			FREE #2
			RETURN #1