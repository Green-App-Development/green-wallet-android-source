// ignore_for_file: lines_longer_than_80_chars

import 'package:chia_crypto_utils/src/clvm/program.dart';

import 'package:chia_crypto_utils/src/nft1.0/puzzles/p2_conditions/p2_conditions.dart';

final p2DelegatedPuzzleOrHiddenPuzzleProgram = Program.deserializeHex(
  'ff02ffff01ff02ffff03ff0bffff01ff02ffff03ffff09ff05ffff1dff0bffff1effff0bff0bffff02ff06ffff04ff02ffff04ff17ff8080808080808080ffff01ff02ff17ff2f80ffff01ff088080ff0180ffff01ff04ffff04ff04ffff04ff05ffff04ffff02ff06ffff04ff02ffff04ff17ff80808080ff80808080ffff02ff17ff2f808080ff0180ffff04ffff01ff32ff02ffff03ffff07ff0580ffff01ff0bffff0102ffff02ff06ffff04ff02ffff04ff09ff80808080ffff02ff06ffff04ff02ffff04ff0dff8080808080ffff01ff0bffff0101ff058080ff0180ff018080',
);


Program solution_for_delegated_puzzle(
    {required Program delegated_puzzle, required Program solution}) {
  return Program.list([Program.list([]), delegated_puzzle, solution]);
}

Program solutionForConditions(Program conditions) {
  final delegatedPuzzle = puzzleForConditions(conditions);

  return solution_for_delegated_puzzle(
    delegated_puzzle: delegatedPuzzle,
    solution: Program.list([]),
  );
}
