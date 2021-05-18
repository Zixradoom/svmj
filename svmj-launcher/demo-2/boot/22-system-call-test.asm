
_systemCallTest:
  ENTER

  ; privilege ring 0
  NO_OP

  LEAVE
  RETURN
.function _systemCallTest
.global   _systemCallTest
.size     _systemCallTest $ - _systemCallTest