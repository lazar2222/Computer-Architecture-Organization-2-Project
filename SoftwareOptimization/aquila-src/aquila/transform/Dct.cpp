/**
 * @file Dct.cpp
 *
 * Discrete Cosine Transform (DCT) calculation.
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

#include "Dct.h"
#include <algorithm>
#include <cmath>
#include <iterator>

namespace Aquila
{
    /**
     * Calculates the Discrete Cosine Transform, type II.
     *
     * See http://en.wikipedia.org/wiki/Discrete_cosine_transform for
     * explanation what DCT-II is.
     *
     * Uses cosine value caching in order to speed up computations.
     *
     * @param data input data vector
     * @param outputLength how many coefficients to return
     * @return vector of DCT coefficients
     */
    std::vector<double> Dct::dct(const std::vector<double>& data, std::size_t outputLength)
    {
        // zero-initialize output vector
        std::vector<double> output(outputLength, 0.0);
        std::size_t inputLength = data.size();

        // DCT scaling factor
        double c0 = std::sqrt(1.0 / inputLength);
        double cn = std::sqrt(2.0 / inputLength);
        // cached cosine values
        double** cosines = getCachedCosines(inputLength, outputLength);

        for (std::size_t n = 0; n < outputLength; ++n)
        {
            std::size_t k;
            register double t=0;
            __builtin_prefetch (&cosines[n][0]);
            __builtin_prefetch (&data[0]);
            __builtin_prefetch (&cosines[n][8]);
            __builtin_prefetch (&data[8]);
            __builtin_prefetch (&cosines[n][16]);
            __builtin_prefetch (&data[16]);
            __builtin_prefetch (&cosines[n][24]);
            __builtin_prefetch (&data[24]);
            for (k = 0; k < inputLength-39; k+=8)
            {
                __builtin_prefetch (&cosines[n][k+32]);
                __builtin_prefetch (&data[k+32]);
                t+=data[k  ] * cosines[n][k  ];
                t+=data[k+1] * cosines[n][k+1];
                t+=data[k+2] * cosines[n][k+2];
                t+=data[k+3] * cosines[n][k+3];
                t+=data[k+4] * cosines[n][k+4];
                t+=data[k+5] * cosines[n][k+5];
                t+=data[k+6] * cosines[n][k+6];
                t+=data[k+7] * cosines[n][k+7];
            }
            for (; k < inputLength; k++)
            {
                t+=data[k] * cosines[n][k];
            }
            output[n]=t;
            output[n] *= (0 == n) ? c0 : cn;    
        }

        return output;
    }

    /**
     * Returns a table of DCT cosine values stored in memory cache.
     *
     * The two params unambigiously identify which cache to use.
     *
     * @param inputLength length of the input vector
     * @param outputLength length of the output vector
     * @return pointer to array of pointers to arrays of doubles
     */
    double** Dct::getCachedCosines(std::size_t inputLength, std::size_t outputLength)
    {
        auto key = std::make_pair(inputLength, outputLength);

        // if we have that key cached, return immediately
        if (cosineCache.find(key) != cosineCache.end())
        {
            return cosineCache[key];
        }

        // nothing in cache for that pair, calculate cosines
        double** cosines = new double*[outputLength];
        for (std::size_t n = 0; n < outputLength; ++n)
        {
            cosines[n] = new double[inputLength];
            for (std::size_t k = 0; k < inputLength; ++k)
            {
                // from the definition of DCT-II
                cosines[n][k] = std::cos((M_PI * (2 * k + 1) * n) /
                                         (2.0 * inputLength));
            }
        }
        cosineCache[key] = cosines;

        return cosines;
    }

    /**
     * Deletes all the memory used by cache.
     */
    void Dct::clearCosineCache()
    {
        for (auto it = std::begin(cosineCache); it != std::end(cosineCache); it++)
        {
            auto key = it->first;
            double** cosines = it->second;
            std::size_t outputLength = key.second;
            for (std::size_t i = 0; i < outputLength; ++i)
            {
                delete [] cosines[i];
            }
            delete [] cosines;
        }
    }
}
