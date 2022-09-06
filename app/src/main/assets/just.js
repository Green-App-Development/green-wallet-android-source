async function test() {
  try {

    var BLS = await Module();

    var value = Android.getArrayStr();
    var prefixAddress = Android.getPrefixForAddress();
    var puzzle_hash=Android.getCurPuzzleHash()

    value = value.slice(1, value.length - 1);
    let arr = value.split(",").map((t) => +t);

    var seed = Uint8Array.from(arr);

    var sk = BLS.AugSchemeMPL.key_gen(seed);
    var pk = sk.get_g1();

    var message = Uint8Array.from([1, 2, 3, 4, 5]);
    var signature = BLS.AugSchemeMPL.sign(sk, message);

    let ok = BLS.AugSchemeMPL.verify(pk, message, signature);
    if (!ok) {
      throw "Mnemonics is not valid";
    }
    console.log(ok); // true
    var skBytes = sk.serialize();
    var pkBytes = pk.serialize();
    var signatureBytes = signature.serialize();
    console.log(BLS.Util.hex_str(skBytes));
    console.log(BLS.Util.hex_str(pkBytes));
    console.log(BLS.Util.hex_str(signatureBytes));
    var masterSk = BLS.AugSchemeMPL.key_gen(seed);
    masterPk = masterSk.get_g1();

//    var sk = BLS.Util.hex_str(skBytes);
    var address = ChiaUtils.puzzle_hash_to_address(puzzle_hash, prefixAddress);
    var fingerPrint = masterPk.get_fingerprint();
    var pk = BLS.Util.hex_str(pkBytes);

    var obj = { address: address, fingerPrint: fingerPrint, pk: pk, sk: puzzle_hash };

    var myJSON = JSON.stringify(obj);
    Android.getJSONWallet(myJSON);
  } catch (error) {
    console.log(error);
    Android.adjustingAssureTxt();
  }
}

async function formParams() {

 try{
      var BLS = await Module();
      var address = Android.getAddressForPuzzle();
      var puzzle_hash=ChiaUtils.address_to_puzzle_hash(address)
      Android.getPuzzleHash(puzzle_hash)
}catch(error){

   console.log(error);
   Android.errorConverting();

}

}
