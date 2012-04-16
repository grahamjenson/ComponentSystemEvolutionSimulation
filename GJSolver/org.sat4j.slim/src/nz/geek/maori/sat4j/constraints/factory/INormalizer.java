package nz.geek.maori.sat4j.constraints.factory;

import java.math.BigInteger;

import nz.geek.maori.sat4j.core.Vec;
import nz.geek.maori.sat4j.core.VecInt;
import nz.geek.maori.sat4j.specs.ContradictionException;
import nz.geek.maori.sat4j.specs.IVec;
import nz.geek.maori.sat4j.specs.IVecInt;
import nz.geek.maori.sat4j.tools.PseudoUtils;

//*******
interface INormalizer {
	PBContainer nice(IVecInt ps, IVec<BigInteger> bigCoefs, boolean moreThan,
			BigInteger bigDeg) throws ContradictionException;

	public static final INormalizer FOR_COMPETITION = new INormalizer() {

		private static final long serialVersionUID = 1L;

		@Override
		public PBContainer nice(IVecInt literals, IVec<BigInteger> coefs,
				boolean moreThan, BigInteger degree)
				throws ContradictionException {
			if (literals.size() != coefs.size()) {
				throw new IllegalArgumentException(
						"Number of coeff and literals are different!!!");
			}
			IVecInt cliterals = new VecInt(literals.size());
			literals.copyTo(cliterals);
			IVec<BigInteger> ccoefs = new Vec<BigInteger>(literals.size());
			coefs.copyTo(ccoefs);
			for (int i = 0; i < cliterals.size();) {
				if (ccoefs.get(i).equals(BigInteger.ZERO)) {
					cliterals.delete(i);
					ccoefs.delete(i);
				} else {
					i++;
				}
			}
			int[] theLits = new int[cliterals.size()];
			cliterals.copyTo(theLits);
			BigInteger[] normCoefs = new BigInteger[ccoefs.size()];
			ccoefs.copyTo(normCoefs);
			BigInteger degRes = PseudoUtils.niceParametersForCompetition(
					theLits, normCoefs, moreThan, degree);
			return new PBContainer(theLits, normCoefs, degRes);

		}

	};

}
