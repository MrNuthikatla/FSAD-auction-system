package com.example.eBaazee.impl;

import com.example.eBaazee.entities.Bid;
import com.example.eBaazee.entities.Product;
import com.example.eBaazee.entities.User;
import com.example.eBaazee.repository.BidRepository;
import com.example.eBaazee.repository.ProductRepository;
import com.example.eBaazee.repository.UserRepository;
import com.example.eBaazee.service.BidService;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BidServiceImpl implements BidService {

    @Autowired
    private BidRepository bidRepo;
    @Autowired
    private ProductRepository productRepo;
    @Autowired
    private UserRepository userRepo;

    @Override
    public Bid placeBid(Long productId, Long userId, double price) {
        Product product = productRepo.findById(productId)
            .orElseThrow(() -> new RuntimeException("Product not found"));
        User user = userRepo.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        if (product.getEndTime().isBefore(LocalDateTime.now())) {
            product.setStatus(Product.ProductStatus.FROZEN);
            productRepo.save(product);
        }

        if (product.getStatus() != Product.ProductStatus.ACTIVE) {
            throw new RuntimeException("Bidding is closed for this product");
        }

        if (price < product.getMinBid() || price > product.getMaxBid()) {
            throw new RuntimeException("Bid must be within min and max bid");
        }

        if (price <= product.getCurrentBidPrice()) {
            throw new RuntimeException("Bid must be higher than the current bid");
        }

        if (bidRepo.existsByProductAndBidder(product, user)) {
            throw new RuntimeException("User has already placed a bid");
        }

        product.setCurrentBidPrice(price);
        productRepo.save(product);

        Bid bid = new Bid();
        bid.setProduct(product);
        bid.setBidder(user);
        bid.setBidPrice(price);
        bid.setBidTime(LocalDateTime.now());

        return bidRepo.save(bid);
    }

    @Override
    public List<Bid> getBidsForProduct(Long productId) {
        Product product = productRepo.findById(productId).orElseThrow();
        return bidRepo.findByProduct(product);
    }

    @Override
    public double getAverageBid(Long productId) {
        List<Bid> bids = getBidsForProduct(productId);
        return bids.stream().mapToDouble(Bid::getBidPrice).average().orElse(0);
    }

    @Override
    public int getBidderCount(Long productId) {
        return getBidsForProduct(productId).size();
    }

    @Override
    public double getHighestBid(Long productId) {
        List<Bid> bids = getBidsForProduct(productId);
        return bids.stream().mapToDouble(Bid::getBidPrice).max().orElse(0);
    }

}
