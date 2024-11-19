function disp_map = dense_match(img_left, img_right, win_size, d_min, d_max, method)

[num_row_right, num_col_right] = size(img_right);
[num_row_left,num_col_left] = size(img_left);

if (num_row_right == num_row_left && num_col_right == num_col_left)
else
    error('Both right and left images should have the same number of rows and columns');
end

img_right = im2double(img_right);
img_left = im2double(img_left);

if (mod(win_size, 2) == 0)
    error('The window size must be an odd number.');
end

if (d_min > d_max)
    error('Minimum Disparity must be less than the Maximum disparity.');
end

disp_map = zeros(num_row_right, num_col_right);

win = (win_size - 1)/2;

maximize = 0;
if strcmp(method, 'NCC')
    maximize = 1;
end

for i = 1 + win : 1 :num_row_right - win   
    for j = 1 + win : 1 : num_col_right - win - d_max

        if(maximize)
            prev_corr_score = 0.0;
        else
            prev_corr_score = 65532;
        end

        best_match_so_far = d_min;
        
        for d = d_min : d_max

            region_right = img_right(i - win : i + win, j - win : j + win);
            region_left = img_left(i - win : i + win, j + d - win : j + d + win);
            
%             mean_right = mean2(region_right);
%             mean_left = mean2(region_left);
            
            tmp_corr_score = zeros(size(region_right));
            
            if strcmp(method,'SAD')
                tmp_corr_score = abs(region_right - region_left);
            elseif strcmp(method,'SSD')
                tmp_corr_score = (region_right - region_left).^2;         
            elseif strcmp(method,'NCC')
                den = sqrt(sum(sum(region_right.^2))*sum(sum(region_left.^2)));
                tmp_corr_score = region_right.*region_left/den;
            end

            corr_score = sum(sum(tmp_corr_score));
            if (maximize)
                if (corr_score > prev_corr_score)
                    prev_corr_score = corr_score;
                    best_match_so_far = d;
                end
            else
                if (prev_corr_score > corr_score)
                    prev_corr_score = corr_score;
                    best_match_so_far = d;
                end
            end
        end
        
        disp_map(i, j) = best_match_so_far;
    end
end

end