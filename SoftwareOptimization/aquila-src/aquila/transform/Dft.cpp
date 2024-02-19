/**
 * @file Dft.cpp
 *
 * A reference implementation of the Discrete Fourier Transform.
 *
 * Note - this is a direct application of the DFT equations and as such it
 * surely isn't optimal. The implementation here serves only as a reference
 * model to compare with.
 *
 * This file is part of the Aquila DSP library.
 * Aquila is free software, licensed under the MIT/X11 License. A copy of
 * the license is provided with the library in the LICENSE file.
 *
 * @package Aquila
 * @version 3.0.0-dev
 * @author Zbigniew Siciarz
 * @date 2007-2014
 * @license http://www.opensource.org/licenses/mit-license.php MIT
 * @since 3.0.0
 */

#include "Dft.h"
#include <algorithm>
#include <cmath>
#include <complex>
#include <emmintrin.h>
#include <immintrin.h>

namespace Aquila
{
    /**
     * Complex unit.
     */
    const ComplexType Dft::j(0, 1);

    /**
     * Applies the transformation to the signal.
     *
     * @param x input signal
     * @return calculated spectrum
     */
    SpectrumType Dft::fft(const SampleType x[])
    {
        SpectrumType spectrum(N);
        ComplexType WN = std::exp((-j) * 2.0 * M_PI / static_cast<double>(N));

        for (unsigned int k = 0; k < N; ++k)
        {
            ComplexType sum(0, 0);
            for (unsigned int n = 0; n < N; ++n)
            {
                sum += x[n] * std::pow(WN, n * k);
            }
            spectrum[k] = sum;
        }

        return spectrum;
    }

    /**
     * Applies the inverse transform to the spectrum.
     *
     * @param spectrum input spectrum
     * @param x output signal
     */
    void Dft::ifft(SpectrumType spectrum, double x[])
    {
        ComplexType WN = std::exp((-j) * 2.0 * M_PI / static_cast<double>(N));
        unsigned int k=0;
        /*double dn=static_cast<double>(N);
        __m256d NS =_mm256_broadcast_sd(&dn);
        for (; k < N-3; k+=4)
        {
            ComplexType sum0(0, 0);
            ComplexType sum1(0, 0);
            ComplexType sum2(0, 0);
            ComplexType sum3(0, 0);
            for (unsigned int n = 0; n < N; ++n)
            {
                sum0 += spectrum[n] * std::pow(WN, -static_cast<int>(n * k));
                sum1 += spectrum[n] * std::pow(WN, -static_cast<int>(n * (k+1)));
                sum2 += spectrum[n] * std::pow(WN, -static_cast<int>(n * (k+2)));
                sum3 += spectrum[n] * std::pow(WN, -static_cast<int>(n * (k+3)));
            }
            __m256d abv=_mm256_set_pd(std::abs(sum0),std::abs(sum1),std::abs(sum2),std::abs(sum3));
            abv=_mm256_div_pd (abv,NS);
            _mm256_storeu_pd(x+k,abv);
        }*/
        for (; k < N; ++k)
        {
            ComplexType sum(0, 0);
            for (unsigned int n = 0; n < N; ++n)
            {
                sum += spectrum[n] * std::pow(WN, -static_cast<int>(n * k));
            }
            x[k] = std::abs(sum) / static_cast<double>(N);
        }
    }
}
