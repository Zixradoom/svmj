
_systemCallStubTest:
  ENTER
  
  MOVE_I r5 ah ; system call 10
  PUSH r5
  CALL_I _systemCallStub
  
  LEAVE
  RETURN
.function _systemCallStubTest
.global   _systemCallStubTest
.size     _systemCallStubTest $ - _systemCallStubTest

.global _systemCallStub