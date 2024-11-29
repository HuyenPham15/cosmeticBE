var swiper = new Swiper(".mySwiper", {
    slidesPerView: 4,
    spaceBetween: 30,
    pagination: {
        el: ".swiper-pagination",
        clickable: true,
    },
    navigation: {
        nextEl: ".swiper-button-next",
        "prevEl": ".swiper-button-prev",
    },
});
document.addEventListener('DOMContentLoaded', function () {
    // Lấy Carousel và các phần tử sản phẩm
    var bannerCarousel = document.querySelector('#bannerCarousel');
    var carousel = new bootstrap.Carousel(bannerCarousel);

    // Lấy tất cả các product-list
    var productLists = document.querySelectorAll('.product-list');

    // Hàm để ẩn tất cả các product-list
    function hideAllProducts() {
        productLists.forEach(function (list) {
            list.classList.add('d-none');
        });
    }

    // Hàm để hiển thị product-list theo banner hiện tại
    function showProducts(bannerId) {
        var productList = document.querySelector('#products-' + bannerId);
        if (productList) {
            productList.classList.remove('d-none');
        }
    }

    // Lắng nghe sự kiện khi slide thay đổi
    bannerCarousel.addEventListener('slide.bs.carousel', function (event) {
        // Lấy slide đang được chuyển tới
        var nextSlide = event.relatedTarget;
        var bannerId = nextSlide.getAttribute('data-products');

        // Ẩn tất cả sản phẩm và hiển thị sản phẩm tương ứng
        hideAllProducts();
        showProducts(bannerId);
    });

    // Khi tải trang, hiển thị sản phẩm cho slide đầu tiên
    var activeSlide = bannerCarousel.querySelector('.carousel-item.active');
    var initialBannerId = activeSlide.getAttribute('data-products');
    hideAllProducts();
    showProducts(initialBannerId);
});
var swiper = new Swiper(".mySwipers", {
    slidesPerView: 5,
    spaceBetween: 30,
    pagination: {
        el: ".swiper-pagination",
        clickable: true,
    },
});
function toggleChatbox() {
    var chatbox = document.getElementById('chatbox');
    if (chatbox.style.display === 'none' || chatbox.style.display === '') {
        chatbox.style.display = 'block';
    } else {
        chatbox.style.display = 'none';
    }
}
// form change pass
function togglePasswordVisibility(inputId, icon) {
    const inputField = document.getElementById(inputId);
    const iconElement = icon.querySelector('i');
    if (inputField.type === "password") {
        inputField.type = "text";
        iconElement.classList.remove('fa-eye');
        iconElement.classList.add('fa-eye-slash');
    } else {
        inputField.type = "password";
        iconElement.classList.remove('fa-eye-slash');
        iconElement.classList.add('fa-eye');
    }
}

// Thay đổi hình ảnh sản phẩm chính khi bấm vào thumbnail
function changeImage(imageSrc) {
    document.getElementById('main-image').src = imageSrc;
}

// Tăng giảm số lượng sản phẩm
function increaseQuantity() {
    var quantityInput = document.getElementById('quantity');
    var quantity = parseInt(quantityInput.value);
    quantityInput.value = quantity + 1;
}

function decreaseQuantity() {
    var quantityInput = document.getElementById('quantity');
    var quantity = parseInt(quantityInput.value);
    if (quantity > 1) {
        quantityInput.value = quantity - 1;
    }
}