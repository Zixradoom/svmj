
_biosSymbolBase:
_biosRamPhysicalBaseAddress:
  .4byte 8H
.object _biosRamPhysicalBaseAddress
.global _biosRamPhysicalBaseAddress
.size _biosRamPhysicalBaseAddress $ - _biosRamPhysicalBaseAddress
  
_biosRamPhysicalSize:
  .4byte 8H
.object _biosRamPhysicalSize
.global _biosRamPhysicalSize
.size _biosRamPhysicalSize $ - _biosRamPhysicalSize

_biosRamPageSize:
  .4byte cH
.object _biosRamPageSize
.global _biosRamPageSize
.size _biosRamPageSize $ - _biosRamPageSize

_biosPageDirectoryEntrySize:
  .4byte 10H
.object _biosPageDirectoryEntrySize
.global _biosPageDirectoryEntrySize
.size _biosPageDirectoryEntrySize $ - _biosPageDirectoryEntrySize

_biosPageDirectoryEntryAddressMask:
  .4byte 14H
.object _biosPageDirectoryEntryAddressMask
.global _biosPageDirectoryEntryAddressMask
.size _biosPageDirectoryEntryAddressMask $ - _biosPageDirectoryEntryAddressMask

_biosPageDirectoryIndexMask:
  .4byte 18H
.object _biosPageDirectoryIndexMask
.global _biosPageDirectoryIndexMask
.size _biosPageDirectoryIndexMask $ - _biosPageDirectoryIndexMask

_biosPageTableEntrySize:
  .4byte 1cH
.object _biosPageTableEntrySize
.global _biosPageTableEntrySize
.size _biosPageTableEntrySize $ - _biosPageTableEntrySize

_biosPageTableEntryAddressMask:
  .4byte 20H
.object _biosPageTableEntryAddressMask
.global _biosPageTableEntryAddressMask
.size _biosPageTableEntryAddressMask $ - _biosPageTableEntryAddressMask

_biosPageTableIndexMask:
  .4byte 24H
.object _biosPageTableIndexMask
.global _biosPageTableIndexMask
.size _biosPageTableIndexMask $ - _biosPageTableIndexMask

_biosFrameAllocatorMark:
.global _biosFrameAllocatorMark
.value _biosFrameAllocatorMark D0H

_biosFrameAllocatorClear:
  .4byte 5cH
.object _biosFrameAllocatorClear
.global _biosFrameAllocatorClear
.size _biosFrameAllocatorClear $ - _biosFrameAllocatorClear

_biosFrameAllocatorCheck:
  .4byte 94H
.object _biosFrameAllocatorCheck
.global _biosFrameAllocatorCheck
.size _biosFrameAllocatorCheck $ - _biosFrameAllocatorCheck

_biosFrameAllocatorFirstFree:
  .4byte d8H
.object _biosFrameAllocatorFirstFree
.global _biosFrameAllocatorFirstFree
.size _biosFrameAllocatorFirstFree $ - _biosFrameAllocatorFirstFree

_biosMapReadPageEntry:
  .4byte 178H
.object _biosMapReadPageEntry
.global _biosMapReadPageEntry
.size _biosMapReadPageEntry $ - _biosMapReadPageEntry

_biosMapWritePageEntry:
  .4byte 1b0H
.object _biosMapWritePageEntry
.global _biosMapWritePageEntry
.size _biosMapWritePageEntry $ - _biosMapWritePageEntry

_biosMapPageRange:
  .4byte 1ecH
.object _biosMapPageRange
.global _biosMapPageRange
.size _biosMapPageRange $ - _biosMapPageRange

.object _biosSymbolBase
.global _biosSymbolBase
.size _biosSymbolBase $ - _biosSymbolBase

_ramInfoBase:
_ramPhysicalBaseAddress:
  .4byte 1F000000H
.object _ramPhysicalBaseAddress
.global _ramPhysicalBaseAddress
.size   _ramPhysicalBaseAddress $ - _ramPhysicalBaseAddress

.object _ramInfoBase
.global _ramInfoBase
.size   _ramInfoBase $ - _ramInfoBase
