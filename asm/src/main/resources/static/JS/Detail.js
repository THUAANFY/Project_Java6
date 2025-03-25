AOS.init();

        // Initialize Slick Slider with optimized settings for smooth infinite looping
        $(document).ready(function(){
            // Initialize with a small delay to ensure all elements are properly loaded
            setTimeout(function() {
                $('.related-products-slider').slick({
                    dots: true,
                    infinite: true, // Enable infinite looping
                    speed: 600, // Moderate speed for smooth transitions
                    cssEase: 'cubic-bezier(0.25, 0.1, 0.25, 1)', // Smooth easing function
                    slidesToShow: 5,
                    slidesToScroll: 1,
                    autoplay: true,
                    autoplaySpeed: 3000,
                    pauseOnHover: true,
                    swipeToSlide: true, // Allow swiping to slide regardless of slidesToScroll
                    // prevArrow: '<button type="button" class="slick-prev"><i class="fas fa-chevron-left"></i></button>',
                    // nextArrow: '<button type="button" class="slick-next"><i class="fas fa-chevron-right"></i></button>',
                    // Important settings for smooth infinite looping
                    waitForAnimate: true, // Wait for animation to complete before allowing next action
                    touchThreshold: 10, // Make swiping more responsive
                    edgeFriction: 0.15, // Reduce friction at edges for smoother looping
                    initialSlide: 0, // Start at the first slide
                    responsive: [
                        {
                            breakpoint: 1200,
                            settings: {
                                slidesToShow: 4,
                                slidesToScroll: 1
                            }
                        },
                        {
                            breakpoint: 992,
                            settings: {
                                slidesToShow: 3,
                                slidesToScroll: 1
                            }
                        },
                        {
                            breakpoint: 768,
                            settings: {
                                slidesToShow: 2,
                                slidesToScroll: 1
                            }
                        },
                        {
                            breakpoint: 576,
                            settings: {
                                slidesToShow: 1,
                                slidesToScroll: 1
                            }
                        }
                    ]
                });
            }, 100);

            // Quantity input handlers
            const quantityInput = document.getElementById('quantity');
            const decreaseBtn = document.getElementById('decrease-qty');
            const increaseBtn = document.getElementById('increase-qty');

            decreaseBtn.addEventListener('click', function() {
                let value = parseInt(quantityInput.value);
                if (value > 1) {
                    quantityInput.value = value - 1;
                }
            });

            increaseBtn.addEventListener('click', function() {
                let value = parseInt(quantityInput.value);
                if (value < 99) {
                    quantityInput.value = value + 1;
                }
            });

            // Validate input on change
            quantityInput.addEventListener('change', function() {
                let value = parseInt(this.value);
                if (isNaN(value) || value < 1) {
                    this.value = 1;
                } else if (value > 99) {
                    this.value = 99;
                }
            });

            // Add simple feedback for add to cart button
            const addToCartBtn = document.querySelector('.btn-add-cart');
            if (addToCartBtn) {
                addToCartBtn.addEventListener('click', function() {
                    if (!this.disabled) {
                        // Show a simple success message
                        const successMessage = document.createElement('div');
                        successMessage.className = 'alert alert-success position-fixed top-0 start-50 translate-middle-x mt-4';
                        successMessage.style.zIndex = '9999';
                        successMessage.style.boxShadow = '0 4px 12px rgba(0,0,0,0.1)';
                        successMessage.style.borderRadius = '6px';
                        successMessage.style.padding = '10px 20px';
                        successMessage.innerHTML = '<i class="fas fa-check-circle me-2"></i> Sản phẩm đã được thêm vào giỏ hàng!';
                        document.body.appendChild(successMessage);
                        
                        setTimeout(() => {
                            successMessage.style.opacity = '0';
                            successMessage.style.transition = 'opacity 0.3s ease';
                            setTimeout(() => {
                                document.body.removeChild(successMessage);
                            }, 300);
                        }, 3000);
                    }
                });
            }
        });